package portsim.simulation.thread;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.simulation.SimulationEngine;
import portsim.simulation.navigation.Step;
import portsim.simulation.session.TerminalSession;

import java.time.Duration;

import static portsim.simulation.thread.ShipThread.Goal.ENTER_AND_DOCK;

@NullMarked
public final class ShipThread implements Runnable {
    private static final int BACKOFF_MILLIS = 100;
    private static final Duration ACK_TIMEOUT = Duration.ofSeconds(10);

    private final Ship ship;
    private final Goal goal;

    private final SimulationEngine engine = SimulationEngine.getInstance();

    private TerminalSession session;
    private Position current;

    public enum Goal {
        ENTER_AND_DOCK,
        UNDOCK_AND_EXIT
    }

    public ShipThread(Ship ship, Goal goal, TerminalSession session, Position start) {
        this.ship = ship;
        this.goal = goal;
        this.session = session;
        this.current = start;
    }

    @Override
    public void run() {
        try {
            // Entering ship waits until its start position is free to be occupied
            if (goal == ENTER_AND_DOCK)
                handleEntry();

            while (true) {
                var navigator = session.getNavigator();
                var step = navigator.nextStep(ship, current, goal);

                // Navigator proposed a move within the terminal
                if (step instanceof Step.Move(Position destination))
                    handleMove(destination);

                // Navigator proposed a transfer to another terminal
                else if (step instanceof Step.Transfer(int direction, Position arrival)) {
                    if (!handleTransfer(direction, arrival)) return;
                }

                else return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            engine.notifyListeners(listener -> listener.onShipThreadFinished(ship, goal));
            engine.reportThreadFinished(goal);
        }
    }

    @SuppressWarnings("BusyWait")
    private void handleEntry() throws InterruptedException {
        var navigator = session.getNavigator();

        while (true) {
            var phaser = new StepPhaser();
            var entered = session.lockedGet(() -> navigator.tryEnter(ship, current, phaser));

            if (entered) {
                phaser.await(ACK_TIMEOUT);
                break;
            }

            Thread.sleep(BACKOFF_MILLIS);
        }
    }

    private void handleMove(Position destination) throws InterruptedException {
        var navigator = session.getNavigator();

        var phaser = new StepPhaser();
        var moveSuccess = session.lockedGet(() -> navigator.tryAdvance(ship, current, destination, phaser));

        if (moveSuccess) {
            current = destination;
            phaser.await(ACK_TIMEOUT);
        }
        else Thread.sleep(BACKOFF_MILLIS);
    }

    // Returns false if terminal transfer shouldn't be attempted again
    private boolean handleTransfer(int direction, Position arrival) throws InterruptedException {
        var navigator = session.getNavigator();
        var idTerminalFrom = session.getTerminal().getIdTerminal();

        var phaser = new StepPhaser();
        var transferSuccess = session.lockedGet(() ->
                navigator.tryTransfer(ship, current, direction, arrival, phaser));

        if (transferSuccess) {
            phaser.await(ACK_TIMEOUT);

            var nextIdTerminal = idTerminalFrom + direction;
            var nextSession = engine.getTerminalSession(nextIdTerminal);

            // Ship has left the port
            if (nextSession.isEmpty())
                return false;

            session = nextSession.get();
            current = arrival;
        }
        else Thread.sleep(BACKOFF_MILLIS);

        return true;
    }
}

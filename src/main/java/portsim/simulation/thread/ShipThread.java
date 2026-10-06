package portsim.simulation.thread;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.simulation.SimulationEngine;
import portsim.simulation.navigation.NavigationAction;
import portsim.simulation.session.TerminalSession;

import static portsim.simulation.thread.ShipThread.Goal.ENTER_AND_DOCK;

@NullMarked
public final class ShipThread implements Runnable {
    private static final int BACKOFF_MILLIS = 100;

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
            var navigator = session.getNavigator();

            // Entering ship waits until its start position is free to be occupied
            if (goal == ENTER_AND_DOCK)
                handleEntry();

            while (true) {
                var action = navigator.nextStep(ship, current, goal);

                // Navigator proposed a move action within the terminal
                if (action instanceof NavigationAction.Move(Position destination))
                    handleMove(destination);

                // Navigator proposed a transfer to another terminal
                else if (action instanceof NavigationAction.Transfer(int direction, Position arrival)) {
                    if (!handleTransfer(direction, arrival)) return;
                }

                else return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            engine.notifyListeners(listener -> listener.onShipThreadFinished(ship, goal));
        }
    }

    @SuppressWarnings("BusyWait")
    private void handleEntry() throws InterruptedException {
        var navigator = session.getNavigator();

        while (true) {
            var entered = session.lockedGet(() -> navigator.tryEnter(ship, current));

            if (entered) break;
            Thread.sleep(BACKOFF_MILLIS);
        }
    }

    private void handleMove(Position destination) throws InterruptedException {
        var navigator = session.getNavigator();

        var moveSuccess = session.lockedGet(() -> navigator.tryAdvance(ship, current, destination));

        if (moveSuccess) {
            current = destination;
            Thread.sleep(computeStepDelay());
        }
        else Thread.sleep(BACKOFF_MILLIS);
    }

    // Returns false if terminal transfer shouldn't be attempted again
    private boolean handleTransfer(int direction, Position arrival) throws InterruptedException {
        var navigator = session.getNavigator();

        var nextIdTerminal = session.getTerminal().getIdTerminal() + direction;
        var nextSession = engine.getTerminalSession(nextIdTerminal);

        // End of the terminal chain is reached
        if (nextSession.isEmpty()) {
            session.locked(() -> session.getNavigator().tryLeave(ship, current));
            return false;
        }

        // Leave the previous terminal
        var leaveSuccess = session.lockedGet(() -> navigator.tryLeave(ship, current));

        if (!leaveSuccess) {
            Thread.sleep(BACKOFF_MILLIS);
            return true;
        }

        // Enter the next terminal
        var nextNavigator = nextSession.get().getNavigator();
        var enterSuccess = nextSession.get().lockedGet(() -> nextNavigator.tryEnter(ship, arrival));

        if (enterSuccess) {
            session = nextSession.get();
            current = arrival;
        }
        else Thread.sleep(BACKOFF_MILLIS);

        return true;
    }

    private int computeStepDelay() {
        return 60_000 / ship.getSpeed();
    }
}

package portsim.simulation.event;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.simulation.thread.ShipThread.Goal;

@NullMarked
public interface SimulationListener {
    default void onShipPlaced(Ship ship, Position pos, int idTerminal) {}

    default void onShipEntered(Ship ship, Position arrival, int idTerminal, Runnable ack) {
        ack.run();
    }

    default void onShipLeft(Ship ship, Position leaving, int idTerminal, Runnable ack) {
        ack.run();
    }

    default void onShipTransferred(Ship ship, int idTerminalFrom, Position from,
                                   int idTerminalTo, Position to, Runnable ack) {
        ack.run();
    }

    default void onShipMoved(Ship ship, Position from, Position to, int idTerminal, Runnable ack) {
        ack.run();
    }

    default void onShipThreadFinished(Ship ship, Goal goal) {}
    default void onSimulationFinished() {}
}

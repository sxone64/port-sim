package portsim.simulation.event;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.simulation.thread.ShipThread.Goal;

@NullMarked
public interface SimulationListener {
    default void onShipEntered(Ship ship, Position arrival, int idTerminal) {}
    default void onShipLeft(Ship ship, Position leaving, int idTerminal) {}

    default void onShipMoved(Ship ship, Position from, Position to, int idTerminal) {}

    default void onShipThreadFinished(Ship ship, Goal goal) {}
    default void onSimulationFinished() {}
}
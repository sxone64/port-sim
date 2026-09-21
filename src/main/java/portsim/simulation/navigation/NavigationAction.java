package portsim.simulation.navigation;

import portsim.model.Position;

public sealed interface NavigationAction {
    // Indicates a ship moving to a new position inside a single terminal
    record Move(Position destination) implements NavigationAction {}

    /*
        Indicates a ship transfering from one terminal to another.

        Positive direction means transfer the ship to the next terminal in port's terminal chain.
        Negative direction means transfer the ship to the previous terminal in port's terminal chain.
        If no next or previous terminal is present, ship leaves the port.

        Upon transfer ship is placed on specified arrival position
     */
    record Transfer(int direction, Position arrival) implements NavigationAction {}

    // Indicates that a ship has reached its goal
    record Completed() implements NavigationAction {}
}

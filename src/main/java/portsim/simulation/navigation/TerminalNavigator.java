package portsim.simulation.navigation;

import org.jetbrains.annotations.NotNull;
import portsim.model.Position;
import portsim.model.Terminal;
import portsim.simulation.thread.ShipThread.Goal;

import static portsim.model.Cell.Type.*;

public final class TerminalNavigator {
    private final Terminal terminal;

    public TerminalNavigator(Terminal terminal) {
        this.terminal = terminal;
    }

    // Proposes the next action based on ship's current position and goal
    public @NotNull NavigationAction nextStep(@NotNull Position current, @NotNull Goal goal) {
        return switch (goal) {
            case ENTER_AND_DOCK -> nextStepEntering(current);
            case UNDOCK_AND_EXIT -> nextStepExiting(current);
        };
    }

    private @NotNull NavigationAction nextStepEntering(Position current) {
        var lastColumn = terminal.getTotalColumns() - 1;
        var cellType = terminal.getCellType(current);

        /*
            Go down until the last cell of the lane is reached.
            Once that cell is reached transfer to the next terminal if no free dock is available
            or go right into TRANSIT_UP lane
         */
        if (cellType == TRANSIT_DOWN) {
            if (current.equals(new Position(3, 0))) {
                if (terminal.getFreeDocks() > 0)
                    return new NavigationAction.Move(new Position(3, 1));
                else
                    return new NavigationAction.Transfer(+1, new Position(0, 0));
            }

            return new NavigationAction.Move(new Position(current.row() + 1, 0));
        }

        /*
            Go up until the entry to the terminal's channel is reached (position (2, 1)).
            Once that cell is reached go into CHANNEL_RIGHT lane if there's a free dock available
            or go up towards the last cell.
            Once the last cell is reached go left into TRANSIT_DOWN if there's a free dock available
            or transfer to the previous terminal
         */
        else if (cellType == TRANSIT_UP) {
            if (current.equals(new Position(2, 1)))
                return new NavigationAction.Move(new Position(2, 2));

            else if (current.equals(new Position(0, 1))) {
                if (terminal.getFreeDocks() > 0)
                    return new NavigationAction.Move(new Position(0, 0));
                else
                    return new NavigationAction.Transfer(-1, new Position(3, 1));
            }
        }

        /*
            Go right until a free dock spot below this lane is found.
            If the last cell in the lane is reached go up into CHANNEL_LEFT lane
         */
        else if (cellType == CHANNEL_RIGHT) {
            var dockBelow = new Position(3, current.column());

            if (!terminal.isOccupied(dockBelow))
                return new NavigationAction.Move(dockBelow);

            return current.column() < lastColumn
                    ? new NavigationAction.Move(new Position(2, current.column() + 1))
                    : new NavigationAction.Move(new Position(1, lastColumn));
        }

        /*
            Go left until a free dock spot above this lane is found.
            Ship goes onto TRANSIT_UP lane if no free dock is found
         */
        else if (cellType == CHANNEL_LEFT) {
            var dockAbove = new Position(0, current.column());

            if (!terminal.isOccupied(dockAbove))
                return new NavigationAction.Move(dockAbove);

            return new NavigationAction.Move(new Position(1, current.column() - 1));
        }

        // Returned once the ship is docked
        return new NavigationAction.Completed();
    }

    // Mostly the same ship movement applies as when the ship's entering
    private @NotNull NavigationAction nextStepExiting(Position current) {
        var lastColumn = terminal.getTotalColumns() - 1;
        var cellType = terminal.getCellType(current);

        if (cellType == TRANSIT_UP) {
            if (current.equals(new Position(0, 1)))
                return new NavigationAction.Transfer(-1, new Position(3, 1));

            return new NavigationAction.Move(new Position(current.row() - 1, current.column()));
        }

        // Undock the ship to CHANNEL_LEFT or CHANNEL_RIGHT lane depending on where the ship is docked
        else if (cellType == DOCK)
            return current.row() == 0
                    ? new NavigationAction.Move(new Position(1, current.column()))
                    : new NavigationAction.Move(new Position(2, current.column()));

        else if (cellType == CHANNEL_RIGHT)
            return current.column() < lastColumn
                    ? new NavigationAction.Move(new Position(2, current.column() + 1))
                    : new NavigationAction.Move(new Position(1, lastColumn));

        else if (cellType == CHANNEL_LEFT)
            return current.column() > 1
                    ? new NavigationAction.Move(new Position(1, current.column() - 1))
                    : new NavigationAction.Move(new Position(0, 1));

        // TODO: Not sure if this is ever returned based on how leaving the port is detected
        return new NavigationAction.Completed();
    }
}

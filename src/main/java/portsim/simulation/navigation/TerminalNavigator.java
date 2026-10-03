package portsim.simulation.navigation;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.Terminal;
import portsim.model.ship.Ship;
import portsim.simulation.thread.ShipThread.Goal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static portsim.model.Cell.Type.*;

@NullMarked
public final class TerminalNavigator {
    private final Terminal terminal;

    // These constants define the channel bounds that are used to determine if overtaking is possible or not
    private final int MIN_RETURN_COLUMN, MAX_RETURN_COLUMN;

    // Holds info about original lane returning positions for ships that are overtaking
    private final Map<Ship, Position> pendingReturn = new ConcurrentHashMap<>();

    private final Map<Ship, Position> pendingUndock = new ConcurrentHashMap<>();

    public TerminalNavigator(Terminal terminal) {
        this.terminal = terminal;

        MIN_RETURN_COLUMN = terminal.getTransitColumns();
        MAX_RETURN_COLUMN = terminal.getTotalColumns() - 1;
    }

    public NavigationAction nextStep(Ship ship, Position current, Goal goal) {
        var returnStep = nextReturnStep(ship, current);

        if (returnStep.isPresent())
            return returnStep.get();

        var action = switch (goal) {
            case ENTER_AND_DOCK -> nextStepEntering(current);
            case UNDOCK_AND_EXIT -> nextStepExiting(current);
        };

        if (action instanceof NavigationAction.Move(Position position)) {
            var currentType = terminal.getCellType(current);

            if (goal == Goal.UNDOCK_AND_EXIT && currentType == DOCK)
                pendingUndock.put(ship, position);

            // We find out here if the ship needs to overtake or not
            else if (!pendingReturn.containsKey(ship)) {
                var overtake = nextOvertakeStep(ship, current, position);

                if (overtake.isPresent())
                    return new NavigationAction.Move(overtake.get());
            }
        }

        return action;
    }

    private Optional<NavigationAction> nextReturnStep(Ship ship, Position current) {
        var returnPos = pendingReturn.get(ship);

        // It's determined that the ship isn't overtaking, so continue with normal lane movement
        if (returnPos == null)
            return Optional.empty();

        var currentType = terminal.getCellType(current);
        var isAdjacentLeft = returnPos.column() == current.column() - 1;
        var isAdjacentRight = returnPos.column() == current.column() + 1;

        /*
            If the return position is adjacent column-wise to the current position
            and the return position isn't occupied, then next step is to finish the overtaking
            by moving to the return position
         */
        if ((isAdjacentLeft || isAdjacentRight) && !terminal.isOccupied(returnPos))
            return Optional.of(new NavigationAction.Move(returnPos));

        int direction;
        if (currentType == CHANNEL_LEFT && isAdjacentRight && current.column() < MAX_RETURN_COLUMN)
            direction = 1;
        else if (currentType == CHANNEL_RIGHT && isAdjacentLeft && current.column() > MIN_RETURN_COLUMN)
            direction = -1;

        /*
            Occupied return position is further down the original lane, or we've hit the channel boundary.
            Ship rotates in overtaking lane and continues moving normally alongside that lane
         */
        else return Optional.empty();

        var position = new Position(current.row(), current.column() + direction);

        /*
            Because the ship is going to move by one cell on the overtake lane,
            we need to update the return position accordingly
         */
        updatePendingReturn(ship, current, returnPos);

        return Optional.of(new NavigationAction.Move(position));
    }

    /*
        Determines if the ship should overtake based on its current and desired positions.
        If desired position isn't occupied, no overtaking is needed
     */
    private Optional<Position> nextOvertakeStep(Ship ship, Position from, Position to) {
        if (!terminal.isOccupied(to))
            return Optional.empty();

        var overtake = computeOvertakePos(from, to);

        if (overtake.isEmpty() || terminal.isOccupied(overtake.get()))
            return Optional.empty();

        var returnPos = computeReturnPos(from, to);

        if (returnPos.isEmpty())
            returnPos = computeFallbackPos(overtake.get(), from);

        returnPos.ifPresent(position -> pendingReturn.put(ship, position));

        return overtake;
    }

    private void updatePendingReturn(Ship ship, Position current, Position oldReturn) {
        var newReturn = shiftReturnPos(current, oldReturn);

        if (newReturn.isPresent())
            pendingReturn.put(ship, newReturn.get());

        else {
            var fallback = computeFallbackPos(current, oldReturn);

            if (fallback.isPresent())
                pendingReturn.put(ship, fallback.get());

            else
                pendingReturn.remove(ship);
        }
    }

    /*
        Return a new return position that retains the previous current/return position gap
        or nothing if the shift goes out of channel bounds
     */
    private Optional<Position> shiftReturnPos(Position current, Position oldReturn) {
        var delta = oldReturn.column() - current.column();
        var newReturnColumn = oldReturn.column() + delta;

        if (newReturnColumn < MIN_RETURN_COLUMN || newReturnColumn > MAX_RETURN_COLUMN)
            return Optional.empty();

        return Optional.of(new Position(oldReturn.row(), newReturnColumn));
    }

    /*
        Return a new return position to which an overtaking ship has to backtrack because overtaking isn't possible
        or nothing if there's not a single free cell in channel bounds to backtrack to
     */
    private Optional<Position> computeFallbackPos(Position current, Position oldReturn) {
        var targetRow = oldReturn.row();
        var currentLaneType = terminal.getCellType(current);

        var delta = (currentLaneType == CHANNEL_RIGHT) ? 1 : -1;
        var targetColumn = current.column() + delta;

        while (targetColumn >= MIN_RETURN_COLUMN && targetColumn <= MAX_RETURN_COLUMN) {
            var position = new Position(targetRow, targetColumn);

            if (!terminal.isOccupied(position))
                return Optional.of(position);

            targetColumn += delta;
        }

        return Optional.empty();
    }

    // Overtake position is always adjacent to desired position row-wise
    private Optional<Position> computeOvertakePos(Position from, Position to) {
        if (from.row() != to.row() || Math.abs(from.column() - to.column()) != 1)
            return Optional.empty();

        if (to.column() < MIN_RETURN_COLUMN || to.column() > MAX_RETURN_COLUMN)
            return Optional.empty();

        return switch (from.row()) {
            case 1 -> Optional.of(new Position(2, to.column()));
            case 2 -> Optional.of(new Position(1, to.column()));
            default -> Optional.empty();
        };
    }

    /*
        Used to determine the initial return position for an overtaking maneuver.
        It's calculated based on current/desired position gap
        (calculated position needs to be inside the channel bounds)
     */
    private Optional<Position> computeReturnPos(Position from, Position to) {
        var delta = to.column() - from.column();
        var returnColumn = to.column() + delta;

        if (returnColumn < MIN_RETURN_COLUMN || returnColumn > MAX_RETURN_COLUMN)
            return Optional.empty();

        return Optional.of(new Position(from.row(), returnColumn));
    }

    private NavigationAction nextStepEntering(Position current) {
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
    private NavigationAction nextStepExiting(Position current) {
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

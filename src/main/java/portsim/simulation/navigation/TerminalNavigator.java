package portsim.simulation.navigation;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.Terminal;
import portsim.model.ship.Ship;
import portsim.model.ship.state.StateShip;
import portsim.service.IncidentService;
import portsim.simulation.SimulationEngine;
import portsim.simulation.thread.ShipThread.Goal;
import portsim.simulation.thread.StepPhaser;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static portsim.model.Cell.Type.*;

@NullMarked
public final class TerminalNavigator {
    // Speed percentage cutoff for ship overtaking
    private static final double OVERTAKE_SPEED_CUTOFF_PERCENT = 0.5;

    private final Terminal terminal;

    // These constants define the bounds used to determine if overtaking is possible or not
    private final int MIN_RETURN_COLUMN, MAX_RETURN_COLUMN;

    // Holds info about original lane returning positions for ships that are overtaking
    private final Map<Ship, Position> pendingReturn = new ConcurrentHashMap<>();

    // If a ship on the right wants to move in front, other ships need to let it proceed
    private final Map<Ship, Position> pendingRightHandMove = new ConcurrentHashMap<>();

    private final SimulationEngine engine = SimulationEngine.getInstance();

    public TerminalNavigator(Terminal terminal) {
        this.terminal = terminal;

        MIN_RETURN_COLUMN = terminal.getTransitColumns();
        MAX_RETURN_COLUMN = terminal.getTotalColumns() - 1;
    }

    public boolean tryEnter(Ship ship, Position arrival, StepPhaser phaser) {
        if (terminal.isOccupied(arrival))
            return false;

        terminal.placeShip(ship, arrival);

        engine.notifyListeners(phaser, (listener, ack) ->
                listener.onShipEntered(ship, arrival, terminal.getIdTerminal(), ack)
        );

        return true;
    }

    public boolean tryAdvance(Ship ship, Position from, Position to, StepPhaser phaser) {
        var returnPos = pendingReturn.get(ship);
        var isReturnMove = to.equals(returnPos);

        if (terminal.isOccupied(to))
            return false;

        if (pendingRightHandMove.containsValue(to)) {
            var pos = pendingRightHandMove.get(ship);

            if (!to.equals(pos))
                return false;

            pendingRightHandMove.remove(ship);
        }

        terminal.moveShip(ship, to);

        var incidentOpt = IncidentService.getInstance()
                .checkIncident(ship, terminal, from, to);

        if (isReturnMove)
            pendingReturn.remove(ship);

        engine.notifyListeners(phaser, (listener, ack) ->
                listener.onShipMoved(ship, from, to, terminal.getIdTerminal(), ack)
        );

        incidentOpt.ifPresent(incident ->
                engine.notifyListeners(phaser, (listener, ack) ->
                        listener.onIncident(incident, ack)
                )
        );

        return true;
    }

    public boolean tryLeave(Ship ship, Position pos, StepPhaser phaser) {
        if (!pos.equals(new Position(0, 1)) && !pos.equals(new Position(3, 0)))
            return false;

        terminal.removeShip(ship);

        engine.notifyListeners(phaser, (listener, ack) ->
                listener.onShipLeft(ship, pos, terminal.getIdTerminal(), ack)
        );

        return true;
    }

    public boolean tryTransfer(Ship ship, Position current,
                               int direction, Position arrival, StepPhaser phaser) {
        var nextIdTerminal = terminal.getIdTerminal() + direction;
        var nextSessionOpt = engine.getTerminalSession(nextIdTerminal);

        // If no next terminal exists, attempt to leave the port entirely
        if (nextSessionOpt.isEmpty())
            return tryLeave(ship, current, phaser);

        var nextSession = nextSessionOpt.get();
        var nextTerminal = nextSession.getTerminal();

        // Ensure the arrival cell in the next terminal is free
        if (nextTerminal.isOccupied(arrival))
            return false;

        terminal.removeShip(ship);
        nextTerminal.placeShip(ship, arrival);

        engine.notifyListeners(phaser, (listener, ack) ->
                listener.onShipTransferred(
                        ship, terminal.getIdTerminal(), current, nextIdTerminal, arrival, ack)
        );

        return true;
    }

    public Step nextStep(Ship ship, Position current, Goal goal) {
        var returnStep = nextReturnStep(ship, current);

        if (returnStep.isPresent())
            return returnStep.get();

        var overtakeOpt = nextOvertakeStep(ship, current);
        if (overtakeOpt.isPresent())
            return overtakeOpt.get();

        var step = switch (goal) {
            case ENTER_AND_DOCK -> nextStepEntering(current);
            case UNDOCK_AND_EXIT -> nextStepExiting(current);
        };

        if (step instanceof Step.Move(Position position)) {
            var currentType = terminal.getCellType(current);

            // Ship is undocking
            if (goal == Goal.UNDOCK_AND_EXIT
                    && (currentType == DOCK_UP || currentType == DOCK_DOWN))
                pendingRightHandMove.put(ship, position);

            // Ship is leaving the channel
            else if (position.equals(new Position(1, 1))
                    && current.equals(new Position(1, 2)))
                pendingRightHandMove.put(ship, position);

            // We find out here if the ship needs to overtake or not
            else if (!pendingReturn.containsKey(ship)) {
                var overtake = nextOvertakeStep(ship, current, position);

                if (overtake.isPresent())
                    return new Step.Move(overtake.get());
            }
        }

        return step;
    }

    private Optional<Step> nextReturnStep(Ship ship, Position current) {
        var returnPos = pendingReturn.get(ship);

        // It's determined that the ship isn't overtaking, so continue with normal lane movement
        if (returnPos == null)
            return Optional.empty();

        var delta = returnPos.column() - current.column();

        /*
            If the return position is adjacent column-wise to the current position
            and the return position isn't occupied, then next step is to finish the overtaking
            by moving to the return position
         */
        if (Math.abs(delta) == 1 && !terminal.isOccupied(returnPos))
            return Optional.of(new Step.Move(returnPos));

        return Optional.empty();
    }

    /*
        Determines if the ship should overtake.

        Commercial ships can only overtake other commercial ships.
        State ships can overtake commercial ships and state ships of lower priority.

        If desired position isn't occupied, no overtaking is needed
     */
    private Optional<Position> nextOvertakeStep(Ship ship, Position from, Position to) {
        if (!terminal.isOccupied(to))
            return Optional.empty();

        // Ship in front occuping the destination position
        var overtakeShipOpt = terminal.getShip(to);

        if (overtakeShipOpt.isPresent()) {
            var overtakeShip = overtakeShipOpt.get();

            // Do not overtake a ship that's already overtaking
            if (pendingReturn.containsKey(overtakeShip))
                return Optional.empty();

            // If the overtaking ship is less than OVERTAKE_SPEED_CUTOFF_PERCENT faster, don't overtake
            if ((double) overtakeShip.getSpeed() / ship.getSpeed() >= (1.0 - OVERTAKE_SPEED_CUTOFF_PERCENT))
                return Optional.empty();

            if (overtakeShipOpt.get() instanceof StateShip overtakeStateShip) {
                // Overtaking ship is also a state ship
                if (ship instanceof StateShip stateShip) {
                    var overtakeStateShipPriority = overtakeStateShip.getPriority();
                    var stateShipPriority = stateShip.getPriority();

                    // Ships of the same priority can overtake each other
                    if (overtakeStateShipPriority.compareTo(stateShipPriority) < 0)
                        return Optional.empty();
                }
                // Commercial ship cannot overtake the state ship
                else return Optional.empty();
            }
        }

        var overtake = computeOvertakePos(from, to);

        if (overtake.isEmpty() || terminal.isOccupied(overtake.get()))
            return Optional.empty();

        var returnPosOpt = computeReturnPos(from, to);

        if (returnPosOpt.isEmpty())
            returnPosOpt = computeFallbackPos(overtake.get(), from);

        returnPosOpt.ifPresent(position -> pendingReturn.put(ship, position));

        return overtake;
    }

    /*
        Used to get the next step for an overtaking ship in the overtake lane.
        Return position is updated after the next step is calculated
     */
    private Optional<Step> nextOvertakeStep(Ship ship, Position current) {
        var returnPos = pendingReturn.get(ship);

        if (returnPos == null)
            return Optional.empty();

        var delta = returnPos.column() - current.column();
        var currentType = terminal.getCellType(current);

        var possible = switch (currentType) {
            case CHANNEL_LEFT -> delta == 1 && current.column() < MAX_RETURN_COLUMN;
            case CHANNEL_RIGHT -> delta == 1 && current.column() > MIN_RETURN_COLUMN;
            default -> false;
        };

        if (!possible)
            return Optional.empty();

        var position = new Position(current.row(), current.column() + delta);

        /*
            Because the ship is going to move by one cell on the overtake lane,
            we need to update the return position accordingly
         */
        updatePendingReturn(ship, current, returnPos);

        return Optional.of(new Step.Move(position));
    }

    private void updatePendingReturn(Ship ship, Position current, Position oldReturnPos) {
        var newReturn = shiftReturnPos(current, oldReturnPos);

        if (newReturn.isPresent())
            pendingReturn.put(ship, newReturn.get());

        else {
            var fallback = computeFallbackPos(current, oldReturnPos);

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
    private Optional<Position> shiftReturnPos(Position current, Position oldReturnPos) {
        var delta = oldReturnPos.column() - current.column();
        var newReturnColumn = oldReturnPos.column() + delta;

        if (newReturnColumn < MIN_RETURN_COLUMN || newReturnColumn > MAX_RETURN_COLUMN)
            return Optional.empty();

        return Optional.of(new Position(oldReturnPos.row(), newReturnColumn));
    }

    /*
        Return a new return position to which an overtaking ship has to backtrack because overtaking isn't possible
        or nothing if there's not a single free cell in channel bounds to backtrack to
     */
    private Optional<Position> computeFallbackPos(Position current, Position oldReturnPos) {
        var targetRow = oldReturnPos.row();

        var delta = current.column() - oldReturnPos.column();
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

    private Step nextStepEntering(Position current) {
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
                    return new Step.Move(new Position(3, 1));
                else
                    return new Step.Transfer(+1, new Position(0, 0));
            }

            return new Step.Move(new Position(current.row() + 1, 0));
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
                return new Step.Move(new Position(2, 2));

            else if (current.equals(new Position(0, 1))) {
                if (terminal.getFreeDocks() > 0)
                    return new Step.Move(new Position(0, 0));
                else
                    return new Step.Transfer(-1, new Position(3, 1));
            }
        }

        /*
            Go right until a free dock spot below this lane is found.
            If the last cell in the lane is reached go up into CHANNEL_LEFT lane
         */
        else if (cellType == CHANNEL_RIGHT) {
            var dockBelow = new Position(3, current.column());

            if (!terminal.isOccupied(dockBelow))
                return new Step.Move(dockBelow);

            return current.column() < lastColumn
                    ? new Step.Move(new Position(2, current.column() + 1))
                    : new Step.Move(new Position(1, lastColumn));
        }

        /*
            Go left until a free dock spot above this lane is found.
            Ship goes onto TRANSIT_UP lane if no free dock is found
         */
        else if (cellType == CHANNEL_LEFT) {
            var dockAbove = new Position(0, current.column());

            if (!terminal.isOccupied(dockAbove))
                return new Step.Move(dockAbove);

            return new Step.Move(new Position(1, current.column() - 1));
        }

        // Returned once the ship is docked
        return new Step.Completed();
    }

    // Mostly the same ship movement applies as when the ship's entering
    private Step nextStepExiting(Position current) {
        var lastColumn = terminal.getTotalColumns() - 1;
        var cellType = terminal.getCellType(current);

        if (cellType == TRANSIT_UP) {
            if (current.equals(new Position(0, 1)))
                return new Step.Transfer(-1, new Position(3, 1));

            return new Step.Move(new Position(current.row() - 1, current.column()));
        }

        // Undock the ship to CHANNEL_LEFT or CHANNEL_RIGHT lane depending on where the ship is docked
        else if (cellType == DOCK_UP || cellType == DOCK_DOWN)
            return current.row() == 0
                    ? new Step.Move(new Position(1, current.column()))
                    : new Step.Move(new Position(2, current.column()));

        else if (cellType == CHANNEL_RIGHT)
            return current.column() < lastColumn
                    ? new Step.Move(new Position(2, current.column() + 1))
                    : new Step.Move(new Position(1, lastColumn));

        else if (cellType == CHANNEL_LEFT)
            return current.column() > 1
                    ? new Step.Move(new Position(1, current.column() - 1))
                    : new Step.Move(new Position(0, 1));

        return new Step.Completed();
    }
}

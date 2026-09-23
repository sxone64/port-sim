package portsim.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import portsim.model.ship.Ship;
import portsim.model.ship.state.StateShip;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import static portsim.model.Cell.Type.*;

public final class Terminal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SHIP_NOT_FOUND = "Specified ship is not part of this terminal";

    private static final List<Cell.Type> CHANNEL_MASK = List.of(CHANNEL_LEFT, CHANNEL_RIGHT);
    private static final List<Cell.Type> TRANSIT_MASK = List.of(TRANSIT_DOWN, TRANSIT_UP);

    // One DOCK row at the top and one at the bottom
    private static final int GRID_ROWS = CHANNEL_MASK.size() + 2;
    private static final int GRID_COLUMNS = 17;

    private final int idTerminal;
    private final Cell [][] grid;

    private final List<Ship> ships;
    private final List<Position> dockPositions;

    /*
        Grid keeps terminal's state for serialization
        and map is used purely to avoid frequent grid traversal
    */
    private transient Map<Ship, Position> shipPositions;

    public Terminal(int idTerminal) {
        this.idTerminal = idTerminal;
        grid = initGrid();

        ships = new ArrayList<>();
        dockPositions = findDockPositions();
        shipPositions = buildShipPositions();
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        shipPositions = buildShipPositions();
    }

    public int getIdTerminal() {
        return idTerminal;
    }

    public @NotNull @Unmodifiable List<Ship> getShips() {
        return List.copyOf(ships);
    }

    public int getStateShipCount() {
        return (int) ships.stream()
                .filter(ship -> ship instanceof StateShip)
                .count();
    }

    public int getNumDocks() {
        return dockPositions.size();
    }

    public int getFreeDocks() {
        return dockPositions.size() - ships.size();
    }

    public @NotNull Optional<Position> getShipPosition(@NotNull Ship ship) {
        return Optional.ofNullable(shipPositions.get(ship));
    }

    public @NotNull @Unmodifiable List<Position> getDockPositions() {
        return List.copyOf(dockPositions);
    }

    public int getTotalColumns() {
        return GRID_COLUMNS;
    }

    public @NotNull Cell.Type getCellType(@NotNull Position position) {
        return getCell(position).getType();
    }

    public boolean isOccupied(@NotNull Position position) {
        return getCell(position).isOccupied();
    }

    /*
        Registers the ship to the terminal and reserves a dock for it without placing it on terminal's grid.

        Registration should be performed before any grid placement occurs if the specified ship is meant
        to dock at the terminal
     */
    public void registerShip(@NotNull Ship ship) {
        if (getFreeDocks() <= 0)
            throw new TerminalFullException(idTerminal);

        ships.add(ship);
    }

    /*
        Remove the ship from the terminal.
        If the ship is registered, it's unregistered from the terminal.
        If the ship is positioned somewhere on terminal's grid, it's removed
     */
    public void removeShip(@NotNull Ship ship) {
        var isRegistered = ships.remove(ship);
        var position = shipPositions.remove(ship);

        if (!isRegistered && position == null)
            throw new NotFoundException(SHIP_NOT_FOUND);

        if (position != null)
            getCell(position).setOccupant(null);
    }

    public void updateShip(@NotNull Ship oldShip, @NotNull Ship newShip) {
        var index = ships.indexOf(oldShip);

        if (index == -1)
            throw new NotFoundException(SHIP_NOT_FOUND);

        ships.set(index, newShip);

        var position = shipPositions.remove(oldShip);
        if (position != null) {
            shipPositions.put(newShip, position);
            getCell(position).setOccupant(newShip);
        }
    }

    /*
        Returns Cell.Type initialized 2D array of Cell objects based on TRANSIT_MASK and CHANNEL_MASK.
        Transit lanes are always on the left side of the grid and follow TRANSIT_MASK column-wise.
        When looking on the right side of the grid, first and last rows are of DOCK type and
        rest follow CHANNEL_MASK types row-wise.
     */
    private Cell @NotNull [][] initGrid() {
        var transitColumns = TRANSIT_MASK.size();
        var grid = new Cell[GRID_ROWS][GRID_COLUMNS];

        for (var col = 0; col < GRID_COLUMNS; col++) {
            Cell.Type type = null;

            var isTransit = col < transitColumns;
            if (isTransit)
                type = TRANSIT_MASK.get(col);

            for (var row = 0; row < GRID_ROWS; row++) {
                if (!isTransit) {
                    if (row == 0 || row == GRID_ROWS - 1) type = DOCK;
                    else type = CHANNEL_MASK.get(row - 1);
                }
                grid[row][col] = new Cell(type);
            }
        }

        return grid;
    }

    private @NotNull List<Position> findDockPositions() {
        var dockPositions = new ArrayList<Position>();

        for (var row = 0; row < GRID_ROWS; row++)
            for (var col = 0; col < GRID_COLUMNS; col++)
                if (grid[row][col].getType() == DOCK)
                    dockPositions.add(new Position(row, col));

        return dockPositions;
    }

    private @NotNull Map<Ship, Position> buildShipPositions() {
        var positions = new HashMap<Ship, Position>();

        for (var row = 0; row < GRID_ROWS; row++)
            for (var col = 0; col < GRID_COLUMNS; col++) {
                var occupant = grid[row][col].getOccupant();

                if (occupant != null)
                    positions.put(occupant, new Position(row, col));
            }

        return positions;
    }

    private Cell getCell(Position position) {
        if (!isInBounds(position))
            throw new NotFoundException("Position %s is not part of this terminal".formatted(position));

        return grid[position.row()][position.column()];
    }

    private boolean isInBounds(@NotNull Position position) {
        return position.row() >= 0 && position.row() < GRID_ROWS
                && position.column() >= 0 && position.column() < GRID_COLUMNS;
    }
}

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

    private static final String SHIP_NOT_FOUND = "Specified ship is not part of terminal ID %d";

    private static final int GRID_ROWS = 4, GRID_COLUMNS = 17, TRANSIT_COLUMNS = 2;

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

    public int getTransitColumns() {
        return TRANSIT_COLUMNS;
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
        to dock at the terminal.
        Unregistered ships can be part of terminal's grid as long as they don't attempt to dock at a DOCK cell
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
            throw new NotFoundException(SHIP_NOT_FOUND.formatted(idTerminal));

        if (position != null)
            getCell(position).setOccupant(null);
    }

    public void updateShip(@NotNull Ship oldShip, @NotNull Ship newShip) {
        var index = ships.indexOf(oldShip);

        if (index == -1)
            throw new NotFoundException(SHIP_NOT_FOUND.formatted(idTerminal));

        ships.set(index, newShip);

        var position = shipPositions.remove(oldShip);
        if (position != null) {
            shipPositions.put(newShip, position);
            getCell(position).setOccupant(newShip);
        }
    }

    public void placeShip(@NotNull Ship ship, @NotNull Position position) {
        var cell = getCell(position);
        checkNotOccupied(cell, position);

        if (cell.getType() == DOCK)
            checkShipRegistered(ship);

        cell.setOccupant(ship);
        shipPositions.put(ship, position);
    }

    public void moveShip(@NotNull Ship ship, @NotNull Position to) {
        var from = shipPositions.get(ship);

        if (from == null)
            throw new NotFoundException(SHIP_NOT_FOUND.formatted(idTerminal));

        var toCell = getCell(to);
        checkNotOccupied(toCell, to);

        if (toCell.getType() == DOCK)
            checkShipRegistered(ship);

        getCell(from).setOccupant(null);
        toCell.setOccupant(ship);
        shipPositions.put(ship, to);
    }

    private Cell @NotNull [][] initGrid() {
        var grid = new Cell[GRID_ROWS][GRID_COLUMNS];

        for (int col = 0; col < GRID_COLUMNS; col++) {
            var isTransitColumn = col < TRANSIT_COLUMNS;
            var transitType = col == 0 ? TRANSIT_DOWN : TRANSIT_UP;

            grid[0][col] = new Cell(isTransitColumn ? transitType : Cell.Type.DOCK);
            grid[1][col] = new Cell(isTransitColumn ? transitType : Cell.Type.CHANNEL_LEFT);
            grid[2][col] = new Cell(isTransitColumn ? transitType : Cell.Type.CHANNEL_RIGHT);
            grid[3][col] = new Cell(isTransitColumn ? transitType : Cell.Type.DOCK);
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
            throw new NotFoundException("Position %s is not part of terminal ID %d".formatted(position, idTerminal));

        return grid[position.row()][position.column()];
    }

    private boolean isInBounds(@NotNull Position position) {
        return position.row() >= 0 && position.row() < GRID_ROWS
                && position.column() >= 0 && position.column() < GRID_COLUMNS;
    }

    private void checkNotOccupied(@NotNull Cell cell, @NotNull Position position) {
        if (cell.isOccupied())
            throw new IllegalStateException(
                    "Cell %s of terminal ID %d is already occupied by a ship".formatted(position, idTerminal));
    }

    private void checkShipRegistered(Ship ship) {
        if (!ships.contains(ship))
            throw new IllegalStateException(
                    "Ship needs to be registered in order for it to dock at terminal ID %d".formatted(idTerminal));
    }
}

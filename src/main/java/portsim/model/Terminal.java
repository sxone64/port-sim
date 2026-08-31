package portsim.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import portsim.model.ship.Ship;
import portsim.model.ship.state.StateShip;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static portsim.model.Cell.Type.*;

public final class Terminal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final List<Cell.Type> CHANNEL_MASK = List.of(CHANNEL_LEFT, CHANNEL_RIGHT);
    private static final List<Cell.Type> TRANSIT_MASK = List.of(TRANSIT_DOWN, TRANSIT_UP);

    // One DOCK row at the top and one at the bottom
    private static final int GRID_ROWS = CHANNEL_MASK.size() + 2;
    private static final int GRID_COLUMNS = 17;

    private final int idTerminal;
    private final Cell [][] grid;

    // Faster lookup to avoid traversing the grid
    private final List<Position> dockPositions;
    private final Map<Ship, Position> shipPositions;

    private int freeDocks;

    public Terminal(int idTerminal) {
        this.idTerminal = idTerminal;
        grid = initGrid();

        dockPositions = findDockPositions();
        shipPositions = new HashMap<>();

        freeDocks = dockPositions.size();
    }

    public int getIdTerminal() {
        return idTerminal;
    }

    public @NotNull @Unmodifiable List<Ship> getShips() {
        return List.copyOf(shipPositions.keySet());
    }

    public int getStateShipCount() {
        var ships = shipPositions.keySet();

        return (int) ships.stream()
                .filter(ship -> ship instanceof StateShip)
                .count();
    }

    public int getNumDocks() {
        return dockPositions.size();
    }

    public int getFreeDocks() {
        return freeDocks;
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
}

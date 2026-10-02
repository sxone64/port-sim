package portsim.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import portsim.model.ship.Ship;

import java.io.Serial;
import java.io.Serializable;

@NullMarked
public final class Cell implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final Type type;
    @Nullable private Ship occupant;

    public enum Type {
        TRANSIT_DOWN, TRANSIT_UP,
        DOCK,
        CHANNEL_LEFT,
        CHANNEL_RIGHT
    }

    public Cell(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public boolean isOccupied() {
        return occupant != null;
    }

    public @Nullable Ship getOccupant() {
        return occupant;
    }

    public void setOccupant(@Nullable Ship occupant) {
        this.occupant = occupant;
    }
}

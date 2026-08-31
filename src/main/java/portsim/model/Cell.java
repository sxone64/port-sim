package portsim.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import portsim.model.ship.Ship;

import java.io.Serial;
import java.io.Serializable;

public final class Cell implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Type type;
    private Ship occupant;

    public enum Type {
        TRANSIT_DOWN, TRANSIT_UP,
        DOCK,
        CHANNEL_LEFT,
        CHANNEL_RIGHT
    }

    public Cell(@NotNull Type type) {
        this.type = type;
    }

    public @NotNull Type getType() {
        return type;
    }

    public boolean isOccupied() {
        return occupant != null;
    }

    public void setOccupant(@Nullable Ship occupant) {
        this.occupant = occupant;
    }
}

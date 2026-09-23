package portsim.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

public record Position(int row, int column) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public @NotNull String toString() {
        return "(%d, %d)".formatted(row, column);
    }
}

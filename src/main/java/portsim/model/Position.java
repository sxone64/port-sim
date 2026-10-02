package portsim.model;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.io.Serializable;

@NullMarked
public record Position(int row, int column) implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "(%d, %d)".formatted(row, column);
    }
}

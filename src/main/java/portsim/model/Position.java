package portsim.model;

import java.io.Serial;
import java.io.Serializable;

public record Position(int row, int column) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}

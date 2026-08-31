package portsim.model.ship;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.nio.file.Path;

public class Cruiser extends Ship {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int numPassengers;

    public Cruiser(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                   int IMO, int speed, @NotNull Path photoPath, int numPassengers) {
        super(name, engineNumber, regNumber, IMO, speed, photoPath);
        this.numPassengers = numPassengers;
    }

    public int getNumPassengers() {
        return numPassengers;
    }
}

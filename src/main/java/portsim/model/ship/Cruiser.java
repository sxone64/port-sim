package portsim.model.ship;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.nio.file.Path;

@NullMarked
public class Cruiser extends Ship {
    @Serial private static final long serialVersionUID = 1L;

    private final int numPassengers;

    public Cruiser(String name, String engineNumber, String regNumber,
                   int imo, int speed, Path photoPath, int numPassengers) {
        super(name, engineNumber, regNumber, imo, speed, photoPath);
        this.numPassengers = numPassengers;
    }

    public int getNumPassengers() {
        return numPassengers;
    }
}

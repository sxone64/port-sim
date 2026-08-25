package portsim.model.ship;

import java.nio.file.Path;

public class Cruiser extends Ship {
    private final int numPassengers;

    public Cruiser(String name, String engineNumber, String regNumber,
                   int IMO, int speed, Path photoPath, int numPassengers) {
        super(name, engineNumber, regNumber, IMO, speed, photoPath);
        this.numPassengers = numPassengers;
    }

    public int getNumPassengers() {
        return numPassengers;
    }
}

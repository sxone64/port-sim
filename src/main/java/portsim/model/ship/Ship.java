package portsim.model.ship;

import java.nio.file.Path;

public abstract class Ship {
    protected final String name, engineNumber, regNumber;
    protected final int imo; // Uniquely identifies the ship
    protected final int speed;
    protected final Path photoPath;

    public Ship(String name, String engineNumber, String regNumber, int imo, int speed, Path photoPath) {
        this.name = name;
        this.engineNumber = engineNumber;
        this.regNumber = regNumber;
        this.imo = imo;
        this.speed = speed;
        this.photoPath = photoPath;
    }

    public String getName() {
        return name;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public int getImo() {
        return imo;
    }

    public int getSpeed() {
        return speed;
    }

    public Path getPhotoPath() {
        return photoPath;
    }
}

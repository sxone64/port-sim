package portsim.model.ship;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public abstract class Ship {
    protected final String name, engineNumber, regNumber;
    protected final int imo; // Uniquely identifies the ship
    protected final int speed;
    protected final String photoPath;

    public Ship(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                int imo, int speed, @NotNull Path photoPath) {
        this.name = name;
        this.engineNumber = engineNumber;
        this.regNumber = regNumber;
        this.imo = imo;
        this.speed = speed;
        this.photoPath = photoPath.toString();
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
        return Path.of(photoPath);
    }
}

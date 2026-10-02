package portsim.model.ship;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;

@NullMarked
public abstract class Ship implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    protected final String name, engineNumber, regNumber;
    protected final int imo; // Uniquely identifies the ship
    protected final int speed;
    protected final String photoPath;

    public Ship(String name, String engineNumber, String regNumber,
                int imo, int speed, Path photoPath) {
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

package portsim.model.ship;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.nio.file.Path;

@NullMarked
public class Tanker extends Ship {
    @Serial private static final long serialVersionUID = 1L;

    private final double volume; // Volume in number of barrels

    public Tanker(String name, String engineNumber, String regNumber,
                  int imo, int speed, Path photoPath, double volume) {
        super(name, engineNumber, regNumber, imo, speed, photoPath);
        this.volume = volume;
    }

    public double getVolume() {
        return volume;
    }
}

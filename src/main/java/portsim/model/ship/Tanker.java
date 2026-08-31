package portsim.model.ship;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.nio.file.Path;

public class Tanker extends Ship {
    @Serial
    private static final long serialVersionUID = 1L;

    private final double volume; // Volume in number of barrels

    public Tanker(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                  int imo, int speed, @NotNull Path photoPath, double volume) {
        super(name, engineNumber, regNumber, imo, speed, photoPath);
        this.volume = volume;
    }

    public double getVolume() {
        return volume;
    }
}

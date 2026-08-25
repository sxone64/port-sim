package portsim.model.ship;

import java.nio.file.Path;

public class Tanker extends Ship {
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

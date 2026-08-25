package portsim.model.ship;

import java.nio.file.Path;

public class ContainerShip extends Ship {
    private final int capacity; // TEU (Twenty-foot Equivalent Unit) - whole number of containers

    public ContainerShip(String name, String engineNumber, String regNumber,
                         int imo, int speed, Path photoPath, int capacity) {
        super(name, engineNumber, regNumber, imo, speed, photoPath);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}

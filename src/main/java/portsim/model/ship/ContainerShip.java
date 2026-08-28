package portsim.model.ship;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ContainerShip extends Ship {
    private final int capacity; // TEU (Twenty-foot Equivalent Unit) - whole number of containers

    public ContainerShip(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                         int imo, int speed, @NotNull Path photoPath, int capacity) {
        super(name, engineNumber, regNumber, imo, speed, photoPath);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}

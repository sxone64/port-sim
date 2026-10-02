package portsim.model.ship.builder;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Ship;

import java.nio.file.Path;

@NullMarked
public final class ContainerShipBuilder<T extends ContainerShip> extends BaseShipBuilder<ContainerShipBuilder<T>> {
    @Nullable private Integer capacity;

    private final ContainerShipFactory<T> factory;

    @FunctionalInterface
    interface ContainerShipFactory<T extends ContainerShip> {
        T create(String name, String engineNumber, String regNumber, int imo,
                 int speed, Path photoPath, int capacity);
    }

    ContainerShipBuilder(ContainerShipFactory<T> factory) {
        this.factory = factory;
    }

    public void capacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public Ship build() {
        requireBaseFieldsSet();

        if (capacity == null)
            throw new IllegalStateException("Container ship builder is missing required capacity field");

        return factory.create(getName(), getEngineNumber(), getRegNumber(), getImo(),
                getSpeed(), getPhotoPath(), capacity);
    }
}

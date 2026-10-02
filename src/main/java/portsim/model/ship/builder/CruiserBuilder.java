package portsim.model.ship.builder;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;

import java.nio.file.Path;

@NullMarked
public final class CruiserBuilder<T extends Cruiser> extends BaseShipBuilder<CruiserBuilder<T>> {
    @Nullable private Integer numPassengers;

    private final CruiserFactory<T> factory;

    @FunctionalInterface
    interface CruiserFactory<T extends Cruiser> {
        T create(String name, String engineNumber, String regNumber, int imo,
                 int speed, Path photoPath, int numPassengers);
    }

    CruiserBuilder(CruiserFactory<T> factory) {
        this.factory = factory;
    }

    public void numPassengers(int numPassengers) {
        this.numPassengers = numPassengers;
    }

    @Override
    public Ship build() {
        requireBaseFieldsSet();

        if (numPassengers == null)
            throw new IllegalStateException("Builder is missing required numPassengers field");

        return factory.create(getName(), getEngineNumber(), getRegNumber(), getImo(),
                getSpeed(), getPhotoPath(), numPassengers);
    }
}

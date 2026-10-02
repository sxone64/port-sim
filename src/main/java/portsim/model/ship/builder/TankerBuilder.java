package portsim.model.ship.builder;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;

import java.nio.file.Path;

@NullMarked
public final class TankerBuilder<T extends Tanker> extends BaseShipBuilder<TankerBuilder<T>> {
    @Nullable private Double volume;

    private final TankerFactory<T> factory;

    @FunctionalInterface
    interface TankerFactory<T extends Tanker> {
        T create(String name, String engineNumber, String regNumber, int imo,
                 int speed, Path photoPath, double volume);
    }

    TankerBuilder(TankerFactory<T> factory) {
        this.factory = factory;
    }

    public void volume(double volume) {
        this.volume = volume;
    }

    @Override
    public Ship build() {
        requireBaseFieldsSet();

        if (volume == null)
            throw new IllegalStateException("Tanker builder is missing required volume field");

        return factory.create(getName(), getEngineNumber(), getRegNumber(), getImo(),
                getSpeed(), getPhotoPath(), volume);
    }
}

package portsim.model.ship.builder;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import portsim.model.ship.Ship;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

@NullMarked
public abstract class BaseShipBuilder<SELF extends BaseShipBuilder<SELF>> {
    @Nullable private String name, engineNumber, regNumber;
    @Nullable private Integer imo, speed;
    @Nullable private Path photoPath;

    public final SELF name(String name) {
        this.name = name;
        return self();
    }

    public final SELF engineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
        return self();
    }

    public final SELF regNumber(String regNumber) {
        this.regNumber = regNumber;
        return self();
    }

    public final SELF imo(int imo) {
        this.imo = imo;
        return self();
    }

    public final SELF speed(int speed) {
        this.speed = speed;
        return self();
    }

    public final SELF photoPath(Path photoPath) {
        this.photoPath = photoPath;
        return self();
    }

    public abstract Ship build();

    String getName() {
        return Objects.requireNonNull(name);
    }

    String getEngineNumber() {
        return Objects.requireNonNull(engineNumber);
    }

    String getRegNumber() {
        return Objects.requireNonNull(regNumber);
    }

    int getImo() {
        return Objects.requireNonNull(imo);
    }

    int getSpeed() {
        return Objects.requireNonNull(speed);
    }

    Path getPhotoPath() {
        return Objects.requireNonNull(photoPath);
    }

    @SuppressWarnings("unchecked")
    private SELF self() {
        return (SELF) this;
    }

    /*
        This method should be the first thing to call inside build() implementations.
        It checks if we skipped the initialization of some field/s.
        Forgetting to initialize a field indicates a bug and IllegalStateException is thrown.
        If this method isn't called, getters would throw NullPointerException on uninitialized fields anyway
     */
    final void requireBaseFieldsSet() {
        var missing = new ArrayList<String>();

        if (name == null) missing.add("name");
        if (engineNumber == null) missing.add("engineNumber");
        if (regNumber == null) missing.add("regNumber");
        if (imo == null) missing.add("imo");
        if (speed == null) missing.add("speed");
        if (photoPath == null) missing.add("photoPath");

        if (!missing.isEmpty())
            throw new IllegalStateException(
                    "Ship builder is missing required base fields: %s".formatted(String.join(", ", missing)));
    }
}

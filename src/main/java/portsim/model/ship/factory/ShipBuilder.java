package portsim.model.ship.factory;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Ship;
import portsim.util.FieldValidationException;
import portsim.util.FieldValidator;

import java.nio.file.Path;

public final class ShipBuilder {
    private final Class<? extends Ship> type;

    private String name, engineNumber, regNumber;
    private Integer imo, speed;
    private Path photoPath;

    private Integer numPassengers;
    private Double volume;
    private Integer capacity;

    private final FieldValidator validator = FieldValidator.getInstance();

    public static @NotNull ShipBuilder of(@NotNull Class<? extends Ship> type) {
        return new ShipBuilder(type);
    }

    private ShipBuilder(Class<? extends Ship> type) {
        this.type = type;
    }

    String getName() {
        return name;
    }

    String getEngineNumber() {
        return engineNumber;
    }

    String getRegNumber() {
        return regNumber;
    }

    int getImo() {
        return imo;
    }

    int getSpeed() {
        return speed;
    }

    Path getPhotoPath() {
        return photoPath;
    }

    int getNumPassengers() {
        return numPassengers;
    }

    double getVolume() {
        return volume;
    }

    int getCapacity() {
        return capacity;
    }

    public ShipBuilder name(@NotNull String name) throws FieldValidationException {
        this.name = validator.requireNonBlank("Name", name);
        return this;
    }

    public ShipBuilder engineNumber(@NotNull String engineNumber) throws FieldValidationException {
        this.engineNumber = validator.requireNonBlank("Engine number", engineNumber);
        return this;
    }

    public ShipBuilder regNumber(@NotNull String regNumber) throws FieldValidationException {
        this.regNumber = validator.requireNonBlank("Registration number", regNumber);
        return this;
    }

    public ShipBuilder imo(@NotNull String imo) throws FieldValidationException {
        this.imo = validator.requireValidImo("IMO", imo);
        return this;
    }

    public ShipBuilder speed(int speed) throws FieldValidationException {
        // TODO
        return this;
    }

    public ShipBuilder photoPath(@NotNull Path photoPath) throws FieldValidationException {
        // TODO
        return this;
    }

    public ShipBuilder numPassengers(@NotNull String numPassengers) throws FieldValidationException {
        this.numPassengers = validator.requirePositiveInt("Number of passengers", numPassengers);
        return this;
    }

    public ShipBuilder volume(@NotNull String volume) throws FieldValidationException {
        this.volume = validator.requirePositiveDouble("Volume", volume);
        return this;
    }

    public ShipBuilder capacity(@NotNull String capacity) throws FieldValidationException {
        this.capacity = validator.requirePositiveInt("Capacity", capacity);
        return this;
    }

    public Ship build() {
        return ShipFactory.getInstance().create(type, this);
    }
}

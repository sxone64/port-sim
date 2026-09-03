package portsim.model.ship.factory;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Ship;
import portsim.util.FieldValidator;

import java.nio.file.Path;
import java.util.Random;

public final class ShipBuilder {
    private static final int MIN_SPEED = 30;
    private static final int MAX_SPEED = 120;

    private final Class<? extends Ship> type;

    private final int speed; // Random value between MIN_SPEED and MAX_SPEED
    private final Path pursuitPath; // Single file for all CoastGuard implementations

    private String name, engineNumber, regNumber;
    private int imo;
    private Path photoPath;

    private int numPassengers;
    private double volume;
    private int capacity;

    private final FieldValidator validator = FieldValidator.getInstance();

    public static @NotNull ShipBuilder of(@NotNull Class<? extends Ship> type) {
        return new ShipBuilder(type);
    }

    private ShipBuilder(Class<? extends Ship> type) {
        this.type = type;

        var random = new Random();
        speed = random.nextInt(MIN_SPEED, MAX_SPEED + 1);

        pursuitPath = Path.of("pursuits.txt");
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

    Path getPursuitPath() {
        return pursuitPath;
    }

    public ShipBuilder name(@NotNull String name) throws FieldValidator.ValidationException {
        this.name = validator.requireNonBlank("Name", name);
        return this;
    }

    public ShipBuilder engineNumber(@NotNull String engineNumber) throws FieldValidator.ValidationException {
        this.engineNumber = validator.requireNonBlank("Engine number", engineNumber);
        return this;
    }

    public ShipBuilder regNumber(@NotNull String regNumber) throws FieldValidator.ValidationException {
        this.regNumber = validator.requireNonBlank("Registration number", regNumber);
        return this;
    }

    public ShipBuilder imo(@NotNull String imo) throws FieldValidator.ValidationException {
        this.imo = validator.requireValidImo("IMO", imo);
        return this;
    }

    public ShipBuilder photoPath(@NotNull Path photoPath) throws FieldValidator.ValidationException {
        this.photoPath = validator.requireNonNull("Photo", photoPath);
        return this;
    }

    public ShipBuilder numPassengers(@NotNull String numPassengers) throws FieldValidator.ValidationException {
        this.numPassengers = validator.requirePositiveInt("Number of passengers", numPassengers);
        return this;
    }

    public ShipBuilder volume(@NotNull String volume) throws FieldValidator.ValidationException {
        this.volume = validator.requirePositiveDouble("Volume", volume);
        return this;
    }

    public ShipBuilder capacity(@NotNull String capacity) throws FieldValidator.ValidationException {
        this.capacity = validator.requirePositiveInt("Capacity", capacity);
        return this;
    }

    // TODO: Implement build method
}

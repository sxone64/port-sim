package portsim.model.ship.factory;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Ship;

import java.nio.file.Path;

public final class ShipBuilder {
    private final Class<? extends Ship> type;

    private String name, engineNumber, regNumber;
    private Integer imo, speed;
    private Path photoPath;

    private Integer numPassengers;
    private Double volume;
    private Integer capacity;

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

    public ShipBuilder name(@NotNull String name) {
        this.name = name;
        return this;
    }

    public ShipBuilder engineNumber(@NotNull String engineNumber) {
        this.engineNumber = engineNumber;
        return this;
    }

    public ShipBuilder regNumber(@NotNull String regNumber) {
        this.regNumber = regNumber;
        return this;
    }

    public ShipBuilder imo(int imo) {
        this.imo = imo;
        return this;
    }

    public ShipBuilder speed(int speed) {
        this.speed = speed;
        return this;
    }

    public ShipBuilder photoPath(@NotNull Path photoPath) {
        this.photoPath = photoPath;
        return this;
    }

    public void numPassengers(int numPassengers) {
        this.numPassengers = numPassengers;
    }

    public void volume(double volume) {
        this.volume = volume;
    }

    public void capacity(int capacity) {
        this.capacity = capacity;
    }

    public Ship build() {
        return ShipFactory.getInstance().create(type, this);
    }
}

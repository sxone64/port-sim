package portsim.model.ship.state.impl;

import portsim.model.ship.Cruiser;
import portsim.model.ship.state.CoastGuard;

import java.nio.file.Path;

public final class GuardCruiser extends Cruiser implements CoastGuard {
    private final Path pursuitPath;
    private boolean sirenOn;

    public GuardCruiser(String name, String engineNumber, String regNumber,
                        int imo, int speed, Path photoPath, int numPassengers, Path pursuitPath) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, numPassengers);
        this.pursuitPath = pursuitPath;
    }

    @Override
    public Path getPursuitPath() {
        return pursuitPath;
    }

    @Override
    public boolean isSirenOn() {
        return sirenOn;
    }

    @Override
    public void setSirenOn(boolean sirenOn) {
        this.sirenOn = sirenOn;
    }
}

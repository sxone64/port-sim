package portsim.model.ship.state.impl;

import portsim.model.ship.Tanker;
import portsim.model.ship.state.CoastGuard;

import java.nio.file.Path;

public final class GuardTanker extends Tanker implements CoastGuard {
    private final Path pursuitPath;
    private boolean sirenOn;

    public GuardTanker(String name, String engineNumber, String regNumber,
                        int imo, int speed, Path photoPath, double volume, Path pursuitPath) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, volume);
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

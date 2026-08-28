package portsim.model.ship.state.impl;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Cruiser;
import portsim.model.ship.state.CoastGuard;

import java.nio.file.Path;

public final class GuardCruiser extends Cruiser implements CoastGuard {
    private final Path pursuitPath;
    private boolean sirenOn;

    public GuardCruiser(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                        int imo, int speed, @NotNull Path photoPath, int numPassengers, @NotNull Path pursuitPath) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, numPassengers);
        this.pursuitPath = pursuitPath;
    }

    @Override
    public @NotNull Path getPursuitPath() {
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

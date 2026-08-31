package portsim.model.ship.state.impl;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.CoastGuard;

import java.nio.file.Path;

public final class GuardTanker extends Tanker implements CoastGuard {
    private final String pursuitPath;
    private boolean sirenOn;

    public GuardTanker(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                       int imo, int speed, @NotNull Path photoPath, double volume, @NotNull Path pursuitPath) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, volume);
        this.pursuitPath = pursuitPath.toString();
    }

    @Override
    public @NotNull Path getPursuitPath() {
        return Path.of(pursuitPath);
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

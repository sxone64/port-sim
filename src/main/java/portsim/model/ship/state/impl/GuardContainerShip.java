package portsim.model.ship.state.impl;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.state.CoastGuard;

import java.io.Serial;
import java.nio.file.Path;

public final class GuardContainerShip extends ContainerShip implements CoastGuard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String pursuitPath;
    private boolean sirenOn;

    public GuardContainerShip(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                              int imo, int speed, @NotNull Path photoPath, int capacity, @NotNull Path pursuitPath) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, capacity);
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

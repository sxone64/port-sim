package portsim.model.ship.state.impl;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.CoastGuard;

import java.io.Serial;
import java.nio.file.Path;

public final class GuardTanker extends Tanker implements CoastGuard {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean sirenOn;

    public GuardTanker(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                       int imo, int speed, @NotNull Path photoPath, double volume) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, volume);
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

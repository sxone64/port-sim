package portsim.model.ship.state.impl;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.FireBrigade;

import java.nio.file.Path;

public final class FireBrigadeTanker extends Tanker implements FireBrigade {
    private boolean sirenOn;

    public FireBrigadeTanker(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
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

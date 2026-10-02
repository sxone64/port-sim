package portsim.model.ship.state.impl;

import org.jspecify.annotations.NullMarked;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.FireBrigade;

import java.io.Serial;
import java.nio.file.Path;

@NullMarked
public final class FireBrigadeTanker extends Tanker implements FireBrigade {
    @Serial private static final long serialVersionUID = 1L;

    private boolean sirenOn;

    public FireBrigadeTanker(String name, String engineNumber, String regNumber,
                             int imo, int speed, Path photoPath, double volume) {
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

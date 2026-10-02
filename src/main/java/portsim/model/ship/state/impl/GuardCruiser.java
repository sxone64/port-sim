package portsim.model.ship.state.impl;

import org.jspecify.annotations.NullMarked;
import portsim.model.ship.Cruiser;
import portsim.model.ship.state.CoastGuard;

import java.io.Serial;
import java.nio.file.Path;

@NullMarked
public final class GuardCruiser extends Cruiser implements CoastGuard {
    @Serial private static final long serialVersionUID = 1L;

    private boolean sirenOn;

    public GuardCruiser(String name, String engineNumber, String regNumber,
                        int imo, int speed, Path photoPath, int numPassengers) {
        super(name, engineNumber, regNumber, imo, speed, photoPath, numPassengers);
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

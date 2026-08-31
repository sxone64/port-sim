package portsim.model.ship.state.impl;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Cruiser;
import portsim.model.ship.state.Customs;

import java.io.Serial;
import java.nio.file.Path;

public final class CustomsCruiser extends Cruiser implements Customs {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean sirenOn;

    public CustomsCruiser(@NotNull String name, @NotNull String engineNumber, @NotNull String regNumber,
                          int imo, int speed, @NotNull Path photoPath, int numPassengers) {
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

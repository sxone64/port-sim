package portsim.model.ship.state.impl;

import portsim.model.ship.Tanker;
import portsim.model.ship.state.Customs;

import java.nio.file.Path;

public final class CustomsTanker extends Tanker implements Customs {
    private boolean sirenOn;

    public CustomsTanker(String name, String engineNumber, String regNumber,
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

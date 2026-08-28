package portsim.model.ship.state;

import org.jetbrains.annotations.NotNull;

public interface StateShip {
    enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    boolean isSirenOn();
    void setSirenOn(boolean sirenOn);
    @NotNull Priority getPriority();
}

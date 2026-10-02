package portsim.model.ship.state;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StateShip {
    enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    boolean isSirenOn();
    void setSirenOn(boolean sirenOn);
    Priority getPriority();
}

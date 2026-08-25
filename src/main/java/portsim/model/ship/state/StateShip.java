package portsim.model.ship.state;

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

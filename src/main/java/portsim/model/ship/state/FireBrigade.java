package portsim.model.ship.state;

import org.jspecify.annotations.NullMarked;

import static portsim.model.ship.state.StateShip.Priority.HIGH;

@NullMarked
public interface FireBrigade extends StateShip {
    default Priority getPriority() {
        return HIGH;
    }
}

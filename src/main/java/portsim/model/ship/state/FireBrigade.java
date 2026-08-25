package portsim.model.ship.state;

import static portsim.model.ship.state.StateShip.Priority.HIGH;

public interface FireBrigade extends StateShip {
    default Priority getPriority() {
        return HIGH;
    }
}

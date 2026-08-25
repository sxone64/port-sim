package portsim.model.ship.state;

import static portsim.model.ship.state.StateShip.Priority.LOW;

public interface Customs extends StateShip {
    default Priority getPriority() {
        return LOW;
    }
}

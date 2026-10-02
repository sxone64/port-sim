package portsim.model.ship.state;

import org.jspecify.annotations.NullMarked;

import static portsim.model.ship.state.StateShip.Priority.LOW;

@NullMarked
public interface Customs extends StateShip {
    default Priority getPriority() {
        return LOW;
    }
}

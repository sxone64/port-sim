package portsim.model.ship.state;

import org.jetbrains.annotations.NotNull;

import static portsim.model.ship.state.StateShip.Priority.LOW;

public interface Customs extends StateShip {
    default @NotNull Priority getPriority() {
        return LOW;
    }
}

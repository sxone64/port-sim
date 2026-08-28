package portsim.model.ship.state;

import org.jetbrains.annotations.NotNull;

import static portsim.model.ship.state.StateShip.Priority.HIGH;

public interface FireBrigade extends StateShip {
    default @NotNull Priority getPriority() {
        return HIGH;
    }
}

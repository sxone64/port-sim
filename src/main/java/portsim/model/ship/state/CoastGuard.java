package portsim.model.ship.state;

import java.nio.file.Path;

import static portsim.model.ship.state.StateShip.Priority.MEDIUM;

public interface CoastGuard extends StateShip {
    Path getPursuitPath();

    default Priority getPriority() {
        return MEDIUM;
    }
}

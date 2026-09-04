package portsim.model.ship.state;

import org.jetbrains.annotations.NotNull;
import portsim.io.PursuitFile;

import static portsim.model.ship.state.StateShip.Priority.MEDIUM;

public interface CoastGuard extends StateShip {
    default @NotNull PursuitFile getPursuitFile() {
        return PursuitFile.getInstance();
    }

    default @NotNull Priority getPriority() {
        return MEDIUM;
    }
}

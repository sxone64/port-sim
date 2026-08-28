package portsim.model.ship.state;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static portsim.model.ship.state.StateShip.Priority.MEDIUM;

public interface CoastGuard extends StateShip {
    @NotNull Path getPursuitPath();

    default @NotNull Priority getPriority() {
        return MEDIUM;
    }
}

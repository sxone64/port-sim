package portsim.model.ship.state;

import org.jspecify.annotations.NullMarked;
import portsim.io.PursuitFile;

import static portsim.model.ship.state.StateShip.Priority.MEDIUM;

@NullMarked
public interface CoastGuard extends StateShip {
    default PursuitFile getPursuitFile() {
        return PursuitFile.getInstance();
    }

    default Priority getPriority() {
        return MEDIUM;
    }
}

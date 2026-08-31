package portsim.ui.viewmodel;

import javafx.beans.property.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import portsim.model.Terminal;
import portsim.service.PortService;

import java.util.List;

public final class AdminViewModel {
    private final IntegerProperty totalShipsProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalFreeDocksProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalDocksProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStateShipsProperty = new SimpleIntegerProperty(0);

    private final StringProperty selectedTerminalProperty =
            new SimpleStringProperty("<Select a terminal>");

    private final BooleanProperty isAddShipBtnDisabledProperty = new SimpleBooleanProperty(true);

    private final PortService portService = PortService.getInstance();

    public ReadOnlyIntegerProperty totalShipsProperty() {
        return totalShipsProperty;
    }

    public ReadOnlyIntegerProperty totalFreeDocksProperty() {
        return totalFreeDocksProperty;
    }

    public ReadOnlyIntegerProperty totalDocksProperty() {
        return totalDocksProperty;
    }

    public ReadOnlyIntegerProperty totalStateShipsProperty() {
        return totalStateShipsProperty;
    }

    public ReadOnlyStringProperty selectedTerminalProperty() {
        return selectedTerminalProperty;
    }

    public ReadOnlyBooleanProperty isAddShipBtnDisabledProperty() {
        return isAddShipBtnDisabledProperty;
    }

    public @NotNull @Unmodifiable List<Terminal> getTerminals() {
        return portService.getTerminals();
    }

    public void refresh() {
        totalShipsProperty.set(portService.getTotalShips());
        totalFreeDocksProperty.set(portService.getTotalFreeDocks());
        totalDocksProperty.set(portService.getTotalDocks());
        totalStateShipsProperty.set(portService.getTotalStateShips());

        // TODO: Refresh terminal's ship list
    }
}

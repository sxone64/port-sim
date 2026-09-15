package portsim.ui.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import portsim.model.Terminal;
import portsim.model.ship.Ship;
import portsim.service.PortService;

import java.util.List;
import java.util.Optional;

public final class AdminViewModel {
    private final IntegerProperty totalShipsProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalFreeDocksProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalDocksProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStateShipsProperty = new SimpleIntegerProperty(0);

    private final StringProperty selectedTerminalProperty =
            new SimpleStringProperty("<Select a terminal>");

    private final BooleanProperty isAddShipBtnDisabledProperty = new SimpleBooleanProperty(true);

    private final ObservableList<Ship> terminalShips = FXCollections.observableArrayList();

    private final PortService portService = PortService.getInstance();

    private Integer idTerminal;

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

    public ObservableList<Ship> getTerminalShips() {
        return terminalShips;
    }

    public @NotNull @Unmodifiable List<Terminal> getTerminals() {
        return portService.getTerminals();
    }

    public @NotNull Optional<Integer> getIdTerminal() {
        return Optional.ofNullable(idTerminal);
    }

    public void refresh() {
        totalShipsProperty.set(portService.getTotalShips());
        totalFreeDocksProperty.set(portService.getTotalFreeDocks());
        totalDocksProperty.set(portService.getTotalDocks());
        totalStateShipsProperty.set(portService.getTotalStateShips());

        refreshTerminalShips();
    }

    public void setTerminal(int idTerminal) {
        this.idTerminal = idTerminal;

        selectedTerminalProperty.set("Terminal %d".formatted(idTerminal));
        checkFreeDocks();

        refreshTerminalShips();
    }

    public void removeShip(Ship ship) {
        if (idTerminal != null) {
            portService.removeShip(idTerminal, ship);
            refresh();
        }
    }

    // If there's no free dock at the current terminal, disable the add ship button
    private void checkFreeDocks() {
        if (idTerminal != null) {
            var freeDocks = portService.getFreeDocks(idTerminal);
            isAddShipBtnDisabledProperty.set(freeDocks <= 0);
        }
    }

    private void refreshTerminalShips() {
        if (idTerminal != null) {
            var ships = portService.getShips(idTerminal);
            terminalShips.setAll(ships);
        }
    }
}

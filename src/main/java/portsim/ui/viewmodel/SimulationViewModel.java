package portsim.ui.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import portsim.io.AppLogger;
import portsim.model.Port;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.service.PortService;
import portsim.simulation.SimulationEngine;
import portsim.simulation.event.SimulationListener;
import portsim.simulation.thread.ShipThread.Goal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@NullMarked
public final class SimulationViewModel implements SimulationListener {
    private final StringProperty eventMessageProperty = new SimpleStringProperty("");

    private final Port port;

    private final SimulationEngine simulationEngine = SimulationEngine.getInstance();

    @Nullable
    private SimulationListener uiListener;

    public SimulationViewModel() {
        simulationEngine.addListener(this);
        port = PortService.getInstance().getPort();
    }

    public ReadOnlyStringProperty eventMessageProperty() {
        return eventMessageProperty;
    }

    public Port getPort() {
        return port;
    }

    public void setUiListener(SimulationListener uiListener) {
        this.uiListener = uiListener;
    }

    public void startSimulation(int minShipsPerTerminal) {
        simulationEngine.start(minShipsPerTerminal);
    }

    @Override
    public void onShipPlaced(Ship ship, Position pos, int idTerminal) {
        var message = "[%s] Ship IMO %d placed at %s in terminal ID %d"
                .formatted(getLocalTimeString(), ship.getImo(), pos, idTerminal);

        dispatch(message, null, ui ->
                ui.onShipPlaced(ship, pos, idTerminal)
        );
    }

    @Override
    public void onShipEntered(Ship ship, Position arrival, int idTerminal, Runnable ack) {
        var message = "[%s] Ship IMO %d entered terminal ID %d"
                .formatted(getLocalTimeString(), ship.getImo(), idTerminal);

        dispatch(message, ack, ui ->
                ui.onShipEntered(ship, arrival, idTerminal, ack)
        );
    }

    @Override
    public void onShipLeft(Ship ship, Position leaving, int idTerminal, Runnable ack) {
        var message = "[%s] Ship IMO %d left terminal ID %d"
                .formatted(getLocalTimeString(), ship.getImo(), idTerminal);

        dispatch(message, ack, ui ->
                ui.onShipLeft(ship, leaving, idTerminal, ack)
        );
    }

    @Override
    public void onShipTransferred(Ship ship, int idTerminalFrom, Position from,
                                  int idTerminalTo, Position to, Runnable ack) {
        var message = "[%s] Ship IMO %d transferred from terminal ID %d to terminal ID %d"
                .formatted(getLocalTimeString(), ship.getImo(), idTerminalFrom, idTerminalTo);

        dispatch(message, ack, ui ->
                ui.onShipTransferred(ship, idTerminalFrom, from, idTerminalTo, to, ack)
        );
    }

    @Override
    public void onShipMoved(Ship ship, Position from, Position to, int idTerminal, Runnable ack) {
        var message = "[%s] Ship IMO %d moved from %s to %s in terminal ID %d"
                .formatted(getLocalTimeString(), ship.getImo(), from, to, idTerminal);

        dispatch(message, ack, ui ->
                ui.onShipMoved(ship, from, to, idTerminal, ack)
        );
    }

    @Override
    public void onShipThreadFinished(Ship ship, Goal goal) {
        var message = switch (goal) {
            case ENTER_AND_DOCK ->
                    "[%s] Ship IMO %d has successfully docked at a terminal"
                            .formatted(getLocalTimeString(), ship.getImo());

            case UNDOCK_AND_EXIT -> "[%s] Ship IMO %d has successfully left the port"
                    .formatted(getLocalTimeString(), ship.getImo());
        };

        dispatch(message, null, ui ->
                ui.onShipThreadFinished(ship, goal)
        );
    }

    @Override
    public void onSimulationFinished() {
        var message = "[%s] Simulation has finished".formatted(getLocalTimeString());
        dispatch(message, null, null);
    }

    private String getLocalTimeString() {
        var formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return formatter.format(LocalTime.now());
    }

    private void dispatch(String message, @Nullable Runnable ack, @Nullable Consumer<SimulationListener> event) {
        try {
            Platform.runLater(() -> {
                eventMessageProperty.set(message);

                // Forward the event to registered UI listener if it's present
                if (event != null && uiListener != null)
                    event.accept(uiListener);

                else if (ack != null)
                    ack.run();
            });
        } catch (IllegalStateException e) {
            AppLogger.getInstance().warning("Couldn't dispatch simulation event to the FX thread", e);
        }
    }
}

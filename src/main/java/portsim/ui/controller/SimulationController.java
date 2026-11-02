package portsim.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.simulation.event.SimulationListener;
import portsim.ui.node.EventsLogNode;
import portsim.ui.node.PortNode;
import portsim.ui.node.SimulationPane;
import portsim.ui.viewmodel.SimulationViewModel;

@NullMarked
public final class SimulationController implements SimulationListener {
    @FXML private SimulationPane simulationPane;

    @FXML private ProgressBar progressBar;

    @FXML private Label shipsEnteredLabel;
    @FXML private Label shipsLeftLabel;

    @FXML private EventsLogNode eventsLogNode;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private PortNode portNode;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private SimulationViewModel viewModel;

    @FXML
    private void initialize() {
        viewModel = new SimulationViewModel();

        portNode = new PortNode(viewModel.getPort());
        simulationPane.setContent(portNode);

        viewModel.setUiListener(this);

        viewModel.eventMessageProperty().addListener((_, _, message) -> {
            if (!message.isEmpty())
                eventsLogNode.addMessage(message);
        });

        progressBar.progressProperty().bind(viewModel.progressProperty());

        shipsEnteredLabel.textProperty().bind(
                viewModel.shipsEnteredProperty().asString()
        );

        shipsLeftLabel.textProperty().bind(
                viewModel.shipsLeftProperty().asString()
        );
    }

    @Override
    public void onShipPlaced(Ship ship, Position pos, int idTerminal) {
        var terminalNode = portNode.getTerminalNode(idTerminal);
        terminalNode.ifPresent(node -> node.enterShip(ship, pos, () -> {}));
    }

    @Override
    public void onShipEntered(Ship ship, Position arrival, int idTerminal, Runnable ack) {
        portNode.getTerminalNode(idTerminal).ifPresentOrElse(
                node -> node.enterShip(ship, arrival, ack), ack);
    }

    @Override
    public void onShipLeft(Ship ship, Position leaving, int idTerminal, Runnable ack) {
        portNode.getTerminalNode(idTerminal)
                .ifPresentOrElse(node -> node.exitShip(ship, ack), ack);
    }

    @Override
    public void onShipTransferred(Ship ship, int idTerminalFrom, Position from,
                                  int idTerminalTo, Position to, Runnable ack) {
        portNode.transferShip(ship, idTerminalFrom, from, idTerminalTo, to, ack);
    }

    @Override
    public void onShipMoved(Ship ship, Position from, Position to, int idTerminal, Runnable ack) {
        portNode.getTerminalNode(idTerminal)
                .ifPresentOrElse(node -> node.moveShip(ship, from, to, ack), ack);
    }
}

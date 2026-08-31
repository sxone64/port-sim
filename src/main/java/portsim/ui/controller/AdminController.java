package portsim.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import portsim.model.ship.Ship;
import portsim.ui.viewmodel.AdminViewModel;

public final class AdminController {
    @FXML private VBox terminalsVBox;
    @FXML private Label totalShipsLabel;
    @FXML private Label totalFreeDocksLabel;
    @FXML private Label totalDocksLabel;
    @FXML private Label totalStateShipsLabel;
    @FXML private Label selectedTerminalLabel;
    @FXML private Button addShipBtn;

    // TODO: Wire terminal's ship list
    @FXML private TableView<Ship> tableView;
    @FXML private TableColumn<Ship, String> nameColumn;
    @FXML private TableColumn<Ship, String> imoColumn;
    @FXML private TableColumn<Ship, String> typeColumn;
    @FXML private TableColumn<Ship, String> regNumberColumn;
    @FXML private TableColumn<Ship, Void> actionsColumn;

    private final AdminViewModel viewModel = new AdminViewModel();

    @FXML
    private void initialize() {
        viewModel.refresh();

        // terminalsVBox setup
        viewModel.getTerminals().forEach(terminal ->
                terminalsVBox.getChildren().add(createTerminalButton(terminal.getIdTerminal()))
        );

        setupBindings();
    }

    private @NotNull Button createTerminalButton(int idTerminal) {
        var button = new Button();

        button.setText("Terminal %d".formatted(idTerminal));
        button.setOnAction(_ -> onTerminalSelected(idTerminal));

        return button;
    }

    private void onTerminalSelected(int idTerminal) {
        // TODO
    }

    private void setupBindings() {
        totalShipsLabel.textProperty().bind(
                viewModel.totalShipsProperty().asString()
        );

        totalFreeDocksLabel.textProperty().bind(
                viewModel.totalFreeDocksProperty().asString()
        );

        totalDocksLabel.textProperty().bind(
                viewModel.totalDocksProperty().asString()
        );

        totalStateShipsLabel.textProperty().bind(
                viewModel.totalStateShipsProperty().asString()
        );

        selectedTerminalLabel.textProperty().bind(
                viewModel.selectedTerminalProperty()
        );

        addShipBtn.disableProperty().bind(
                viewModel.isAddShipBtnDisabledProperty()
        );
    }

    @FXML
    private void onAddShipAction() {
        // TODO
    }

    @FXML
    private void onStartSimulationAction() {
        // TODO
    }
}

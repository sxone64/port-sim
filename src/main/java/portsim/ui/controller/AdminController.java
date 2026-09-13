package portsim.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import portsim.io.AppResources;
import portsim.model.ship.Ship;
import portsim.ui.viewmodel.AdminViewModel;
import portsim.ui.viewmodel.ShipFormViewModel;

import java.io.IOException;

import static javafx.geometry.Pos.CENTER;
import static javafx.stage.Modality.APPLICATION_MODAL;

public final class AdminController {
    @FXML private VBox terminalsVBox;
    @FXML private Label totalShipsLabel;
    @FXML private Label totalFreeDocksLabel;
    @FXML private Label totalDocksLabel;
    @FXML private Label totalStateShipsLabel;
    @FXML private Label selectedTerminalLabel;
    @FXML private Button addShipBtn;

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
        setupTable();
    }

    private @NotNull Button createTerminalButton(int idTerminal) {
        var button = new Button();

        button.setText("Terminal %d".formatted(idTerminal));
        button.setOnAction(_ -> onTerminalSelected(idTerminal));

        return button;
    }

    private void onTerminalSelected(int idTerminal) {
        viewModel.setTerminal(idTerminal);
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

    private void setupTable() {
        tableView.setItems(viewModel.getTerminalShips());

        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getName())
        );

        imoColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getImo()))
        );

        typeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getClass().getSimpleName())
        );

        regNumberColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getRegNumber())
        );

        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, updateButton, deleteButton);

            {
                container.setAlignment(CENTER);

                updateButton.setOnAction(_ -> {
                    // TODO
                });

                deleteButton.setOnAction(_ -> {
                    // TODO
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) setGraphic(null);
                else setGraphic(container);
            }
        });
    }

    @FXML
    private void onAddShipAction() {
        var idTerminal = viewModel.getIdTerminal()
                .orElseThrow(() -> new IllegalStateException("Terminal not selected"));

        var loader = new FXMLLoader(AppResources.getInstance().getShipFormFxml());

        Scene scene;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the FXML resource", e);
        }

        ShipFormController controller = loader.getController();
        controller.initialize(new ShipFormViewModel(idTerminal));

        var stage = new Stage();

        stage.initModality(APPLICATION_MODAL);
        stage.initOwner(addShipBtn.getScene().getWindow());
        stage.setTitle("Add ship - Terminal %d".formatted(idTerminal));
        stage.setScene(scene);

        stage.setResizable(false);
        stage.showAndWait();

        viewModel.refresh();
    }

    @FXML
    private void onStartSimulationAction() {
        // TODO
    }
}

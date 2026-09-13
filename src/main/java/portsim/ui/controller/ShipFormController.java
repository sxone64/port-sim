package portsim.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.impl.*;
import portsim.ui.viewmodel.ShipFormViewModel;

import java.util.Map;

import static javafx.scene.control.Alert.AlertType.ERROR;

public final class ShipFormController {
    // Values are used to get UI friendly names instead of relying on e.g. class' simple name
    private static final Map<Class<? extends Ship>, String> STATE_SHIPS = Map.of(
            CustomsCruiser.class, "Customs cruiser",
            CustomsTanker.class, "Customs tanker",
            FireBrigadeTanker.class, "Fire brigade tanker",
            GuardContainerShip.class, "Guard container ship",
            GuardCruiser.class, "Guard cruiser",
            GuardTanker.class, "Guard tanker"
    );

    private static final Map<Class<? extends Ship>, String> COMMERCIAL_SHIPS = Map.of(
            ContainerShip.class, "Container ship",
            Cruiser.class, "Cruiser",
            Tanker.class, "Tanker"
    );

    @FXML private ToggleButton commercialToggleBtn;
    @FXML private ToggleGroup shipTypeGroup;
    @FXML private ToggleButton stateToggleBtn;
    @FXML private ComboBox<Class<? extends Ship>> shipTypeCombo;

    @FXML private TextField nameField;
    @FXML private TextField imoField;
    @FXML private TextField regNumberField;
    @FXML private TextField engineNumberField;

    @FXML private TextField photoField;
    @FXML private Button clearBtn;

    @FXML private Label shipTypeLabel;
    @FXML private VBox specificFieldsVBox;

    @FXML private Button cancelBtn;
    @FXML private Button confirmBtn;

    private ShipFormViewModel viewModel;

    // This method is called after the FXML resource is loaded
    public void initialize(@NotNull ShipFormViewModel viewModel) {
        this.viewModel = viewModel;

        viewModel.setOnConfirmFailed(message -> {
            var alert = new Alert(ERROR);

            alert.setTitle("Input error");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.showAndWait();
        });

        setupGeneralBindings();
        setupShipTypeCombo();

        updateShipTypeCombo();
        confirmBtn.setText("Add ship");
    }

    private void setupGeneralBindings() {
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        imoField.textProperty().bindBidirectional(viewModel.imoProperty());
        regNumberField.textProperty().bindBidirectional(viewModel.regNumberProperty());
        engineNumberField.textProperty().bindBidirectional(viewModel.engineNumberProperty());

        photoField.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    var photoPath = viewModel.photoPathProperty().getValue();
                    if (photoPath == null) return "No photo";
                    else return photoPath.getFileName().toString();
                },
                viewModel.photoPathProperty())
        );

        clearBtn.visibleProperty().bind(viewModel.isClearPhotoPathEnabled());
        clearBtn.managedProperty().bind(viewModel.isClearPhotoPathEnabled());
    }

    private void setupShipTypeCombo() {
        shipTypeGroup.selectedToggleProperty().addListener(
                (_, oldValue, newValue) -> {
                    if (newValue == null) oldValue.setSelected(true);
                    else updateShipTypeCombo();
                });

        shipTypeCombo.setCellFactory(_ -> new ListCell<>() {

            @Override
            protected void updateItem(Class<? extends Ship> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) setText(null);
                else setText(getTypeName(item));
            }
        });

        shipTypeCombo.setButtonCell(new ListCell<>() {

            @Override
            protected void updateItem(Class<? extends Ship> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) setText(null);
                else setText(getTypeName(item));
            }
        });

        shipTypeCombo.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newType) -> {
                    if (newType != null)
                        updateSpecificFields(newType);
                }
        );
    }

    private void updateShipTypeCombo() {
        shipTypeCombo.getItems().setAll(commercialToggleBtn.isSelected()
                ? COMMERCIAL_SHIPS.keySet()
                : STATE_SHIPS.keySet()
        );

        shipTypeCombo.getSelectionModel().selectFirst();
    }

    private String getTypeName(Class<? extends Ship> type) {
        return commercialToggleBtn.isSelected() ? COMMERCIAL_SHIPS.get(type) : STATE_SHIPS.get(type);
    }

    private void updateSpecificFields(@NotNull Class<? extends Ship> type) {
        specificFieldsVBox.getChildren().clear();
        var children = specificFieldsVBox.getChildren();

        shipTypeLabel.setText("%s specific fields".formatted(getTypeName(type)));

        if (Cruiser.class.isAssignableFrom(type))
            children.add(buildSpecificField("Number of passengers", viewModel.numPassengersProperty()));

        if (Tanker.class.isAssignableFrom(type))
            children.add(buildSpecificField("Volume [barrels]", viewModel.volumeProperty()));

        if (ContainerShip.class.isAssignableFrom(type))
            children.add(buildSpecificField("Capacity [TEU]", viewModel.capacityProperty()));

        if (specificFieldsVBox.getScene() != null && specificFieldsVBox.getScene().getWindow() != null) {
            var stage = (Stage) specificFieldsVBox.getScene().getWindow();
            stage.sizeToScene();
        }
    }

    /*
        Constructs a new VBox containing a Label and a TextField.
        This method also performs bidirectional binding of the TextField with a property from the view model
     */
    private @NotNull VBox buildSpecificField(String labelText, StringProperty property) {
        var label = new Label(labelText);

        var textField = new TextField();
        textField.textProperty().bindBidirectional(property);

        return new VBox(label, textField);
    }

    @FXML
    private void onClearAction() {
        viewModel.setPhotoPath(null);
    }

    @FXML
    private void onBrowseAction() {
        var chooser = new FileChooser();

        chooser.setTitle("Select a photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        var file = chooser.showOpenDialog(photoField.getScene().getWindow());

        if (file != null)
            viewModel.setPhotoPath(file.toPath());
    }

    @FXML
    private void onCancelAction() {
        var stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onConfirmAction() {
        var type = shipTypeCombo.getSelectionModel().getSelectedItem();
        var success = viewModel.addShip(type);

        if (success) {
            var stage = (Stage) confirmBtn.getScene().getWindow();
            stage.close();
        }
    }
}

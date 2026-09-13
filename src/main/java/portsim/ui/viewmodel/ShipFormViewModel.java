package portsim.ui.viewmodel;

import javafx.beans.property.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import portsim.io.AppResources;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.factory.ShipBuilder;
import portsim.service.PortService;
import portsim.util.FieldValidationException;

import java.nio.file.Path;
import java.util.Random;
import java.util.function.Consumer;

import static portsim.util.FieldValidator.MAX_SPEED;
import static portsim.util.FieldValidator.MIN_SPEED;

public final class ShipFormViewModel {
    private final StringProperty nameProperty = new SimpleStringProperty("");
    private final StringProperty imoProperty = new SimpleStringProperty("");
    private final StringProperty regNumberProperty = new SimpleStringProperty("");
    private final StringProperty engineNumberProperty = new SimpleStringProperty("");
    private final ObjectProperty<Path> photoPathProperty = new SimpleObjectProperty<>();

    private final BooleanProperty isClearPhotoPathEnabled = new SimpleBooleanProperty(false);

    private final StringProperty numPassengersProperty = new SimpleStringProperty("");
    private final StringProperty volumeProperty = new SimpleStringProperty("");
    private final StringProperty capacityProperty = new SimpleStringProperty("");

    private final PortService portService = PortService.getInstance();
    private final Path defaultPhotoPath = AppResources.getInstance().getDefaultPhotoPath();

    private final int idTerminal;
    private Consumer<String> onConfirmFailed = _ -> {};

    public ShipFormViewModel(int idTerminal) {
        this.idTerminal = idTerminal;
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public StringProperty imoProperty() {
        return imoProperty;
    }

    public StringProperty regNumberProperty() {
        return regNumberProperty;
    }

    public StringProperty engineNumberProperty() {
        return engineNumberProperty;
    }

    public ReadOnlyObjectProperty<Path> photoPathProperty() {
        return photoPathProperty;
    }

    public ReadOnlyBooleanProperty isClearPhotoPathEnabled() {
        return isClearPhotoPathEnabled;
    }

    public StringProperty numPassengersProperty() {
        return numPassengersProperty;
    }

    public StringProperty volumeProperty() {
        return volumeProperty;
    }

    public StringProperty capacityProperty() {
        return capacityProperty;
    }

    public void setOnConfirmFailed(@Nullable Consumer<String> onConfirmFailed) {
        this.onConfirmFailed = onConfirmFailed;
    }

    public void setPhotoPath(@Nullable Path photoPath) {
        photoPathProperty.set(photoPath);
        isClearPhotoPathEnabled.set(photoPath != null);
    }

    public boolean addShip(@NotNull Class<? extends Ship> type) {
        try {
            var ship = buildShip(type);

            portService.addShip(idTerminal, ship);

            return true;
        } catch (FieldValidationException e) {
            onConfirmFailed.accept(e.getMessage());
            return false;
        }
    }

    private Ship buildShip(Class<? extends Ship> type) throws FieldValidationException {
        var builder = ShipBuilder.of(type)
                .name(nameProperty.getValue())
                .engineNumber(engineNumberProperty.getValue())
                .regNumber(regNumberProperty.getValue())
                .imo(imoProperty.getValue())
                .speed(getRandomSpeed());

        var photoPath = photoPathProperty.getValue();
        builder.photoPath(photoPath == null ? defaultPhotoPath : photoPath);

        if (Cruiser.class.isAssignableFrom(type))
            builder.numPassengers(numPassengersProperty.getValue());

        else if (Tanker.class.isAssignableFrom(type))
            builder.volume(volumeProperty.getValue());

        else if (ContainerShip.class.isAssignableFrom(type))
            builder.capacity(capacityProperty.getValue());

        return builder.build();
    }

    private int getRandomSpeed() {
        var random = new Random();
        return random.nextInt(MIN_SPEED, MAX_SPEED + 1);
    }
}

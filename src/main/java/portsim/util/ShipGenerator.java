package portsim.util;

import org.jetbrains.annotations.NotNull;
import portsim.io.AppResources;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.factory.ShipBuilder;
import portsim.model.ship.factory.ShipCategory;
import portsim.model.ship.state.impl.*;
import portsim.service.PortService;

import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

import static portsim.util.FieldValidator.*;

public final class ShipGenerator {
    private static final ShipGenerator INSTANCE = new ShipGenerator();

    private static final Path DEFAULT_PHOTO = AppResources.getInstance().getDefaultPhotoPath();

    public static ShipGenerator getInstance() {
        return INSTANCE;
    }

    private final Random random = new Random();

    private final PortService portService = PortService.getInstance();

    private ShipGenerator() {}

    public @NotNull Ship generateStateShip() {
        return generate(ShipCategory.STATE);
    }

    public @NotNull Ship generateCommercialShip() {
        return generate(ShipCategory.COMMERCIAL);
    }

    private Ship generate(@NotNull ShipCategory category) {
        var types = category.types();
        var type = types.get(random.nextInt(types.size()));

        var id = UUID.randomUUID().toString().substring(0, 6);

        int imo;
        do {
            imo = random.nextInt(MIN_IMO, MAX_IMO + 1);
        } while (portService.isImoTaken(imo));

        try {
            var builder = ShipBuilder.of(type)
                    .name("GEN-%s".formatted(id))
                    .engineNumber("ENG-%s".formatted(id))
                    .regNumber("REG-%s".formatted(id))
                    .imo(String.valueOf(imo))
                    .speed(random.nextInt(MIN_SPEED, MAX_SPEED + 1))
                    .photoPath(DEFAULT_PHOTO);

            if (Cruiser.class.isAssignableFrom(type))
                builder.numPassengers(
                        String.valueOf(random.nextInt(MIN_NUM_PASSENGERS, MAX_NUM_PASSENGERS)));

            else if (Tanker.class.isAssignableFrom(type))
                builder.volume(
                        String.valueOf(random.nextInt(MIN_VOLUME, MAX_VOLUME)));

            else if (ContainerShip.class.isAssignableFrom(type))
                builder.capacity(
                        String.valueOf(random.nextInt(MIN_CAPACITY, MAX_CAPACITY)));

            return builder.build();
        } catch (FieldValidationException e) {
            throw new IllegalStateException("Ship field validation failed", e);
        }
    }
}

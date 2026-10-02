package portsim.util;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import portsim.io.AppResources;
import portsim.model.ship.Ship;
import portsim.model.ship.ShipTypes;
import portsim.model.ship.builder.*;
import portsim.service.PortService;

import java.nio.file.Path;
import java.util.List;
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
        return generate(ShipTypes.state());
    }

    public @NotNull Ship generateCommercialShip() {
        return generate(ShipTypes.commercial());
    }

    private @NonNull Ship generate(@NonNull List<Class<? extends Ship>> types) {
        var type = types.get(random.nextInt(types.size()));

        var id = UUID.randomUUID().toString().substring(0, 6);

        int imo;
        do {
            imo = random.nextInt(MIN_IMO, MAX_IMO + 1);
        } while (portService.isImoTaken(imo));

        var builder = ShipBuilder.of(type)
                .name("GEN-%s".formatted(id))
                .engineNumber("ENG-%s".formatted(id))
                .regNumber("REG-%s".formatted(id))
                .imo(imo)
                .speed(random.nextInt(MIN_SPEED, MAX_SPEED + 1))
                .photoPath(DEFAULT_PHOTO);

        switch (builder) {
            case ContainerShipBuilder<?> b ->
                    b.capacity(random.nextInt(MIN_CAPACITY, MAX_CAPACITY));
            case CruiserBuilder<?> b ->
                    b.numPassengers(random.nextInt(MIN_NUM_PASSENGERS, MAX_NUM_PASSENGERS));
            case TankerBuilder<?> b ->
                    b.volume(random.nextInt(MIN_VOLUME, MAX_VOLUME));
            default -> throw new IllegalStateException(
                    "Type %s doesn't have a builder implementation".formatted(type.getSimpleName()));
        }

        return builder.build();
    }
}

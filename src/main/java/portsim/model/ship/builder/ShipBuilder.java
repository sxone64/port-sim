package portsim.model.ship.builder;

import org.jspecify.annotations.NullMarked;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.impl.*;

import java.util.Map;
import java.util.function.Supplier;

@NullMarked
public final class ShipBuilder {
    private static final Map<Class<? extends Ship>, Supplier<BaseShipBuilder<?>>> BUILDERS = Map.of(
            CustomsCruiser.class, ShipBuilder::customsCruiser,
            CustomsTanker.class, ShipBuilder::customsTanker,
            FireBrigadeTanker.class, ShipBuilder::fireBrigadeTanker,
            GuardContainerShip.class, ShipBuilder::guardContainerShip,
            GuardCruiser.class, ShipBuilder::guardCruiser,
            GuardTanker.class, ShipBuilder::guardTanker,
            ContainerShip.class, ShipBuilder::containerShip,
            Cruiser.class, ShipBuilder::cruiser,
            Tanker.class, ShipBuilder::tanker
    );

    private ShipBuilder() {}

    public static BaseShipBuilder<?> of(Class<? extends Ship> type) {
        var supplier = BUILDERS.get(type);

        if (supplier == null)
            throw new IllegalArgumentException("Unsupported ship type %s".formatted(type.getSimpleName()));

        return supplier.get();
    }

    // Helpers to improve code readability
    private static CruiserBuilder<CustomsCruiser> customsCruiser() {
        return new CruiserBuilder<>(CustomsCruiser::new);
    }

    private static TankerBuilder<CustomsTanker> customsTanker() {
        return new TankerBuilder<>(CustomsTanker::new);
    }

    private static TankerBuilder<FireBrigadeTanker> fireBrigadeTanker() {
        return new TankerBuilder<>(FireBrigadeTanker::new);
    }

    private static ContainerShipBuilder<GuardContainerShip> guardContainerShip() {
        return new ContainerShipBuilder<>(GuardContainerShip::new);
    }

    private static CruiserBuilder<GuardCruiser> guardCruiser() {
        return new CruiserBuilder<>(GuardCruiser::new);
    }

    private static TankerBuilder<GuardTanker> guardTanker() {
        return new TankerBuilder<>(GuardTanker::new);
    }

    private static ContainerShipBuilder<ContainerShip> containerShip() {
        return new ContainerShipBuilder<>(ContainerShip::new);
    }

    private static CruiserBuilder<Cruiser> cruiser() {
        return new CruiserBuilder<>(Cruiser::new);
    }

    private static TankerBuilder<Tanker> tanker() {
        return new TankerBuilder<>(Tanker::new);
    }
}

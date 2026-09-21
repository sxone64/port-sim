package portsim.model.ship.factory;

import org.jetbrains.annotations.NotNull;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.impl.*;

import java.util.List;

public enum ShipCategory {
    COMMERCIAL(List.of(
            ContainerShip.class,
            Cruiser.class,
            Tanker.class
    )),
    STATE(List.of(
            CustomsCruiser.class,
            CustomsTanker.class,
            FireBrigadeTanker.class,
            GuardContainerShip.class,
            GuardCruiser.class,
            GuardTanker.class
    ));

    private final List<Class<? extends Ship>> types;

    ShipCategory(List<Class<? extends Ship>> types) {
        this.types = List.copyOf(types);
    }

    public List<Class<? extends Ship>> types() {
        return types;
    }

    public static @NotNull ShipCategory of(Class<? extends Ship> type) {
        for (var category: values())
            if (category.types.contains(type))
                return category;

        throw new IllegalArgumentException("Unknown ship type %s".formatted(type.getSimpleName()));
    }
}

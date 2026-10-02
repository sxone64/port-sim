package portsim.model.ship;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import portsim.model.ship.state.impl.*;

import java.util.List;

@NullMarked
public final class ShipTypes {
    private static final List<Class<? extends Ship>> STATE = List.of(
            CustomsCruiser.class,
            CustomsTanker.class,
            FireBrigadeTanker.class,
            GuardContainerShip.class,
            GuardCruiser.class,
            GuardTanker.class
    );

    private static final List<Class<? extends Ship>> COMMERCIAL = List.of(
            ContainerShip.class,
            Cruiser.class,
            Tanker.class
    );

    private ShipTypes() {}

    public static @Unmodifiable List<Class<? extends Ship>> commercial() {
        return List.copyOf(COMMERCIAL);
    }

    public static @Unmodifiable List<Class<? extends Ship>> state() {
        return List.copyOf(STATE);
    }

    public static boolean isState(Class<? extends Ship> type) {
        return STATE.contains(type);
    }
}

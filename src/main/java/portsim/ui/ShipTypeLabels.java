package portsim.ui;

import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.impl.*;

import java.util.Map;

public final class ShipTypeLabels {
    private static final Map<Class<? extends Ship>, String> LABELS = Map.ofEntries(
            Map.entry(CustomsCruiser.class, "Customs cruiser"),
            Map.entry(CustomsTanker.class, "Customs tanker"),
            Map.entry(FireBrigadeTanker.class, "Fire brigade tanker"),
            Map.entry(GuardContainerShip.class, "Guard container ship"),
            Map.entry(GuardCruiser.class, "Guard cruiser"),
            Map.entry(GuardTanker.class, "Guard tanker"),
            Map.entry(ContainerShip.class, "Container ship"),
            Map.entry(Cruiser.class, "Cruiser"),
            Map.entry(Tanker.class, "Tanker")
    );

    private ShipTypeLabels() {}

    public static String of(Class<? extends Ship> type) {
        return LABELS.getOrDefault(type, type.getSimpleName());
    }
}

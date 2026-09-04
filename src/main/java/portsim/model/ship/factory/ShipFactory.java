package portsim.model.ship.factory;

import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.impl.*;

import java.util.Map;
import java.util.function.Function;

public final class ShipFactory {
    private static final ShipFactory INSTANCE = new ShipFactory();

    private static final Map<Class<? extends Ship>, Function<ShipBuilder, Ship>> FACTORIES = Map.of(
            Cruiser.class,
            builder -> new Cruiser(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getNumPassengers()
            ),

            Tanker.class,
            builder -> new Tanker(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getVolume()
            ),

            ContainerShip.class,
            builder -> new ContainerShip(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getCapacity()
            ),

            CustomsCruiser.class,
            builder -> new CustomsCruiser(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getNumPassengers()
            ),

            GuardCruiser.class,
            builder -> new GuardCruiser(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getNumPassengers()
            ),

            CustomsTanker.class,
            builder -> new CustomsTanker(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getVolume()
            ),

            FireBrigadeTanker.class,
            builder -> new FireBrigadeTanker(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getVolume()
            ),

            GuardTanker.class,
            builder -> new GuardTanker(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getVolume()
            ),

            GuardContainerShip.class,
            builder -> new GuardContainerShip(
                    builder.getName(),
                    builder.getEngineNumber(),
                    builder.getRegNumber(),
                    builder.getImo(),
                    builder.getSpeed(),
                    builder.getPhotoPath(),
                    builder.getCapacity()
            )
    );

    static ShipFactory getInstance() {
        return INSTANCE;
    }

    public static class TypeNotSupportedException extends RuntimeException {
        public TypeNotSupportedException(String message) {
            super(message);
        }
    }

    private ShipFactory() {}

    Ship create(Class<? extends Ship> type, ShipBuilder builder) {
        var factory = FACTORIES.get(type);

        if (factory == null)
            throw new TypeNotSupportedException("Ship type not supported");

        return factory.apply(builder);
    }
}

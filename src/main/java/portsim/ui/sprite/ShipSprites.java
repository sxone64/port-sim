package portsim.ui.sprite;

import javafx.scene.image.Image;
import org.jspecify.annotations.NullMarked;
import portsim.io.AppResources;
import portsim.model.ship.ContainerShip;
import portsim.model.ship.Cruiser;
import portsim.model.ship.Ship;
import portsim.model.ship.Tanker;
import portsim.model.ship.state.impl.*;

import java.util.Map;
import java.util.Random;

@NullMarked
public final class ShipSprites {
    private static final ShipSprites INSTANCE = new ShipSprites();

    private static final String ROOT = "ship";

    private static final Map<Class<? extends Ship>, String> FIXED = Map.ofEntries(
            Map.entry(CustomsCruiser.class, "cruiser/customs/customs-cruiser.png"),
            Map.entry(CustomsTanker.class, "tanker/customs/customs-tanker.png"),
            Map.entry(FireBrigadeTanker.class, "tanker/fire-brigade/fire-tanker.png"),
            Map.entry(GuardContainerShip.class, "container/guard/guard-container.png"),
            Map.entry(GuardCruiser.class, "cruiser/guard/guard-cruiser.png"),
            Map.entry(GuardTanker.class, "tanker/guard/guard-tanker.png")
    );

    private record VariantSpec(String prefix, int count) {}

    private static final Map<Class<? extends Ship>, VariantSpec> VARIANTS = Map.ofEntries(
            Map.entry(ContainerShip.class, new VariantSpec("container/container", 5)),
            Map.entry(Cruiser.class, new VariantSpec("cruiser/cruiser", 5)),
            Map.entry(Tanker.class, new VariantSpec("tanker/tanker", 5))
    );

    public static ShipSprites getInstance() {
        return INSTANCE;
    }

    private ShipSprites() {}

    // Picks a random sprite for commercial ships and picks the fixed one for state ships
    public Image of(Class<? extends Ship> type) {
        var appResources = AppResources.getInstance();
        var spriteCache = SpriteCache.getInstance();

        var fixed = FIXED.get(type);

        if (fixed != null) {
            var spriteUrl = appResources.getSprite(ROOT, fixed);
            return spriteCache.getImage(spriteUrl);
        }

        var spec = VARIANTS.get(type);
        if (spec != null) {
            var spriteUrl = appResources.getSprite(ROOT, randomVariantFilename(spec));
            return spriteCache.getImage(spriteUrl);
        }

        throw new IllegalArgumentException("No sprite registered for type %s".formatted(type.getSimpleName()));
    }

    private String randomVariantFilename(VariantSpec spec) {
        var variant = new Random().nextInt(1, spec.count() + 1);
        return "%s%d.png".formatted(spec.prefix(), variant);
    }
}

package portsim.ui.sprite;

import javafx.scene.image.Image;
import org.jspecify.annotations.NullMarked;
import portsim.io.AppResources;
import portsim.model.Cell.Type;

import java.util.Map;

import static portsim.model.Cell.Type.*;

@NullMarked
public final class CellSprites {
    private static final CellSprites INSTANCE = new CellSprites();

    private static final String ROOT = "port";

    // Transit cells and channel cells currently share the same sprite
    private final Map<Type, String> FIXED = Map.of(
            TRANSIT_DOWN, "water.png",
            TRANSIT_UP, "water.png",
            CHANNEL_LEFT, "water.png",
            CHANNEL_RIGHT, "water.png",
            DOCK_UP, "dock-up.png",
            DOCK_DOWN, "dock-down.png"
    );

    public static CellSprites getInstance() {
        return INSTANCE;
    }

    private CellSprites() {}

    public Image of(Type type) {
        var appResources = AppResources.getInstance();
        var spriteCache = SpriteCache.getInstance();

        var filename = FIXED.get(type);

        if (filename == null)
            throw new IllegalArgumentException("No sprite registered for type %s".formatted(type));

        var spriteUrl = appResources.getSprite(ROOT, filename);
        return spriteCache.getImage(spriteUrl);
    }
}

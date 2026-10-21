package portsim.ui.sprite;

import javafx.scene.image.Image;
import org.jspecify.annotations.NullMarked;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Caches decoded PNG images based on sprite URLs retrieved from AppResources
@NullMarked
public final class SpriteCache {
    private static final SpriteCache INSTANCE = new SpriteCache();

    private final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    public static SpriteCache getInstance() {
        return INSTANCE;
    }

    private SpriteCache() {}

    public Image getImage(URL spriteUrl) {
        return CACHE.computeIfAbsent(spriteUrl.toString(), Image::new);
    }
}

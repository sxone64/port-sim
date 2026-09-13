package portsim.io;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.file.Path;
import java.util.MissingResourceException;

public final class AppResources {
    private static final AppResources INSTANCE = new AppResources();

    private final URL ADMIN_FXML = requireResource("/fxml/admin.fxml");
    private final URL SHIP_FORM_FXML = requireResource("/fxml/ship-form.fxml");

    private final Path DEFAULT_PHOTO_PATH = resourcePath("/images/default.png");

    public static AppResources getInstance() {
        return INSTANCE;
    }

    private AppResources() {}

    public URL getAdminFxml() {
        return ADMIN_FXML;
    }

    public URL getShipFormFxml() {
        return SHIP_FORM_FXML;
    }

    public Path getDefaultPhotoPath() {
        return DEFAULT_PHOTO_PATH;
    }

    private @NotNull URL requireResource(String path) {
        var resource = AppResources.class.getResource(path);

        if (resource == null)
            throw new MissingResourceException(
                    "Specified resource not found",
                    AppResources.class.getName(),
                    path);

        return resource;
    }

    private @NotNull Path resourcePath(String path) {
        try {
            return Path.of(requireResource(path).toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't resolve resource path", e);
        }
    }
}

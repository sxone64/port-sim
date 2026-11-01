package portsim.io;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.net.URL;
import java.util.function.Function;

@NullMarked
public final class FXMLLoaders {
    private static final String FXML_LOAD_FAILED = "Failed to load the FXML resource";

    public record View<C>(Scene scene, C controller) {}

    private FXMLLoaders() {}

    public static <C> View<C> load(URL fxml) {
        var loader = new FXMLLoader(fxml);

        try {
            var scene = new Scene(loader.load());
            return new View<>(scene, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException(FXML_LOAD_FAILED, e);
        }
    }

    public static Scene loadScene(URL fxml) {
        return load(fxml).scene();
    }
}

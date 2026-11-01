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
        return loadWith(fxml, Scene::new);
    }

    public static <C> View<C> load(URL fxml, double width, double height) {
        return loadWith(fxml, parent -> new Scene(parent, width, height));
    }

    public static Scene loadScene(URL fxml) {
        return load(fxml).scene();
    }

    public static Scene loadScene(URL fxml, double width, double height) {
        return load(fxml, width, height).scene();
    }

    private static <C> View<C> loadWith(URL fxml, Function<Parent, Scene> sceneFactory) {
        var loader = new FXMLLoader(fxml);

        try {
            Parent root = loader.load();
            return new View<>(sceneFactory.apply(root), loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException(FXML_LOAD_FAILED, e);
        }
    }
}

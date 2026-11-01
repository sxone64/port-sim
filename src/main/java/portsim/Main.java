package portsim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.jspecify.annotations.NullMarked;
import portsim.io.AppLogger;
import portsim.io.AppResources;
import portsim.io.FXMLLoaders;

import static javafx.scene.control.Alert.AlertType.ERROR;

@NullMarked
public class Main extends Application {
    @Override
    public void start(Stage stage) {
        var logger = AppLogger.getInstance();

        logger.setOnSevereError((_, _) -> {
            var alert = new Alert(ERROR);

            alert.setTitle("Severe Error");
            alert.setHeaderText("An unrecoverable error has occurred.");
            alert.setContentText("See error.log for details.");

            alert.showAndWait();

            Platform.exit();
        });

        // All exceptions that aren't caught are logged as severe
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                AppLogger.getInstance().severe(
                        "Uncaught exception in %s".formatted(thread.getName()),
                        throwable)
        );

        var scene = FXMLLoaders.loadScene(AppResources.getInstance().getAdminFxml());

        stage.setScene(scene);
        stage.show();

        // For simplicity's sake we cannot downsize the window below initial window size
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
    }
}

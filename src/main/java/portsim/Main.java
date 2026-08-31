package portsim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import portsim.io.AppLogger;

import java.io.IOException;

import static javafx.scene.control.Alert.AlertType.ERROR;

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

        // All exception that aren't caught are logged as severe
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                AppLogger.getInstance().severe(
                        "Uncaught exception in %s".formatted(thread.getName()),
                        throwable)
        );

        var loader = new FXMLLoader(getClass().getResource("/fxml/admin.fxml"));

        Scene scene;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            logger.severe("Failed to load the FXML resource", e);
            return;
        }

        stage.setScene(scene);
        stage.show();

        // For simplicity's sake we cannot downsize the window below initial window size
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
    }
}

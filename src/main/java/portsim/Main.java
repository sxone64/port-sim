package portsim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import portsim.io.AppLogger;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        var logger = AppLogger.getInstance();

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

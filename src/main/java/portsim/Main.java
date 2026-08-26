package portsim;

import javafx.application.Application;
import javafx.stage.Stage;
import portsim.io.AppLogger;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // All exception that aren't caught are logged as severe
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                AppLogger.getInstance().severe(
                        "Uncaught exception in %s".formatted(thread.getName()),
                        throwable)
        );
    }
}

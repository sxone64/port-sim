package portsim;

import portsim.io.AppLogger;

public class Main {
    static void main() {
        // All exception that aren't caught are logged as severe
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                AppLogger.getInstance().severe(
                        "Uncaught exception in %s".formatted(thread.getName()),
                        throwable)
        );
    }
}

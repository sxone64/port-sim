package portsim.io;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.logging.Level.*;

public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger(AppLogger.class.getName());
    private static final AppLogger INSTANCE = new AppLogger();

    public static AppLogger getInstance() {
        return INSTANCE;
    }

    private BiConsumer<String, Throwable> onSevereError = (_, _) -> {};

    // Introduced to avoid multiple threads executing onSevereError
    private final AtomicBoolean shutdownStarted = new AtomicBoolean(false);

    private AppLogger() {
        try {
            var errorHandler = new FileHandler("error.log", true);
            errorHandler.setFormatter(new SimpleFormatter());
            errorHandler.setLevel(ALL);
            errorHandler.setFilter(record -> !record.getLevel().equals(WARNING));
            LOGGER.addHandler(errorHandler);

            var warningHandler = new FileHandler("warning.log", true);
            warningHandler.setFormatter(new SimpleFormatter());
            warningHandler.setLevel(WARNING);
            warningHandler.setFilter(record -> record.getLevel().equals(WARNING));
            LOGGER.addHandler(warningHandler);

            // Warnings are also logged on console
            var consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(WARNING);
            consoleHandler.setFilter(record -> record.getLevel().equals(WARNING));
            LOGGER.addHandler(consoleHandler);

            LOGGER.setUseParentHandlers(false);
            LOGGER.setLevel(ALL);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public void setOnSevereError(BiConsumer<String, Throwable> onSevereError) {
        this.onSevereError = onSevereError;
    }

    public void severe(String message, Throwable t) {
        LOGGER.log(SEVERE, message, t);

        if (shutdownStarted.compareAndSet(false, true))
            onSevereError.accept(message, t);
    }

    public void warning(String message, Throwable t) {
        LOGGER.log(WARNING, message, t);
    }
}

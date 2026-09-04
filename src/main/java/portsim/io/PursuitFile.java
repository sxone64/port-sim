package portsim.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardWatchEventKinds.*;

public final class PursuitFile {
    private static final PursuitFile INSTANCE = new PursuitFile();

    private static final Duration WATCH_DEBOUNCE = Duration.ofMillis(150);

    public static PursuitFile getInstance() {
        return INSTANCE;
    }

    private final Path path = Path.of("pursuit.txt");
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final AppLogger logger = AppLogger.getInstance();

    private List<Integer> imoList;

    private PursuitFile() {
        createIfAbsent();
        read();
        startWatcher();
    }

    // Multiple threads can get the IMO list at the same time if write lock isn't acquired upon reading the file
    public @NotNull @Unmodifiable List<Integer> getImoList() {
        lock.readLock().lock();
        try {
            return List.copyOf(imoList);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void createIfAbsent() {
        try {
            if (Files.notExists(path))
                Files.createFile(path);
        } catch (Exception e) {
            logger.severe("Failed to create the pursuit file", e);
        }
    }

    private void read() {
        var newImoList = new ArrayList<Integer>();

        try {
            var lines = Files.readAllLines(path);

            for (var i = 0; i < lines.size(); i++) {
                var line = lines.get(i);

                if (!line.isBlank()) {
                    try {
                        newImoList.add(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                        logger.warning("Couldn't parse the value at line %d of the pursuit file"
                                .formatted(i + 1), e);
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to read the pursuit file", e);
            return;
        }

        lock.writeLock().lock();
        try {
            imoList = newImoList;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void startWatcher() {
        var parent = path.toAbsolutePath().getParent();
        var fileName = path.getFileName();

        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            parent.register(watchService, ENTRY_MODIFY, ENTRY_DELETE);
        } catch (IOException e) {
            logger.warning("Failed to start the pursuit file watcher", e);
            return;
        }

        var watchThread = createWatchThread(watchService, fileName);
        watchThread.start();
    }

    /*
        Operating system generates two events for a single ENTRY_MODIFY event
        so we need to do the reading only after the second event.

        Upon taking the first event we flip pendingChange to true and attempt to exhaust the event queue via poll.
        If no other event arrives in WATCH_DEBOUNCE time we attempt the reading
    */
    private @NotNull Thread createWatchThread(WatchService watchService, Path fileName) {
        var thread = new Thread(() -> {
            var pendingChange = false;

            while (true) {
                WatchKey key;
                try {
                    if (pendingChange)
                        key = watchService.poll(WATCH_DEBOUNCE.toMillis(), TimeUnit.MILLISECONDS);
                    else
                        key = watchService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ClosedWatchServiceException e) {
                    break;
                }

                if (key == null) {
                    read();
                    pendingChange = false;
                    continue;
                }

                for (var event: key.pollEvents()) {
                    if (event.kind() == OVERFLOW)
                        continue;

                    var changed = (Path) event.context();
                    if (changed != null && changed.equals(fileName))
                        pendingChange = true;
                }

                var valid = key.reset();
                if (!valid)
                    break;
            }
        }, "pursuit-file-watcher");

        thread.setDaemon(true);
        return thread;
    }
}

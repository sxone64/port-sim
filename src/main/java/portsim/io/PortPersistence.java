package portsim.io;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import portsim.model.Port;
import portsim.model.Terminal;

import java.io.*;
import java.util.ArrayList;

public final class PortPersistence {
    private static final PortPersistence INSTANCE = new PortPersistence();

    private static final String FILE_PATH = "port.ser";

    public static PortPersistence getInstance() {
        return INSTANCE;
    }

    private PortPersistence() {}

    public Port loadOrCreate() {
        var file = new File(FILE_PATH);

        if (file.exists()) {
            try (var in = new ObjectInputStream(new FileInputStream(file))) {
                return (Port) in.readObject();
            } catch (Exception e) {
                AppLogger.getInstance().severe("Failed to deserialize the Port object.", e);
            }
        }

        return createFresh();
    }

    public void savePort(@NotNull Port port) {
        try (var out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(port);
        } catch (Exception e) {
            AppLogger.getInstance().severe("Failed to serialize the Port object.", e);
        }
    }

    @Contract(" -> new")
    private @NotNull Port createFresh() {
        var properties = AppProperties.getInstance();
        var numTerminals = properties.getNumTerminals();

        var terminals = new ArrayList<Terminal>();

        for (int i = 0; i < numTerminals; i++)
            terminals.add(new Terminal(i + 1));

        return new Port(terminals);
    }
}

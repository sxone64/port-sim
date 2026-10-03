package portsim.simulation;

import org.jspecify.annotations.NullMarked;
import portsim.service.PortService;
import portsim.simulation.event.SimulationListener;
import portsim.simulation.session.TerminalSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@NullMarked
public final class SimulationEngine {
    private static final SimulationEngine INSTANCE = new SimulationEngine();

    public static SimulationEngine getInstance() {
        return INSTANCE;
    }

    // One session per terminal identified with terminal's ID
    private final Map<Integer, TerminalSession> sessions = new HashMap<>();

    // We don't add new listeners often, and we need concurrent access to the list
    private final List<SimulationListener> listeners = new CopyOnWriteArrayList<>();

    private final PortService portService = PortService.getInstance();

    private SimulationEngine() {}

    public Optional<TerminalSession> getTerminalSession(int idTerminal) {
        return Optional.ofNullable(sessions.get(idTerminal));
    }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(Consumer<SimulationListener> notification) {
        for (var listener: listeners)
            notification.accept(listener);
    }

    private void initSessions() {
        for (var terminal: portService.getTerminals())
            sessions.put(terminal.getIdTerminal(), new TerminalSession(terminal));
    }
}

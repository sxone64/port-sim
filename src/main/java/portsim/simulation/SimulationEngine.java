package portsim.simulation;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.Terminal;
import portsim.model.ship.Ship;
import portsim.model.ship.state.StateShip;
import portsim.service.PortService;
import portsim.simulation.event.SimulationListener;
import portsim.simulation.session.TerminalSession;
import portsim.simulation.thread.ShipThread;
import portsim.simulation.thread.ShipThread.Goal;
import portsim.util.ShipGenerator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static portsim.simulation.thread.ShipThread.Goal.ENTER_AND_DOCK;
import static portsim.simulation.thread.ShipThread.Goal.UNDOCK_AND_EXIT;

@NullMarked
public final class SimulationEngine {
    private static final SimulationEngine INSTANCE = new SimulationEngine();

    // Percentage amount of ships that are selected to undock and exit the port
    private static final double EXIT_SELECTION_RATIO = 0.15;

    // How many commercial ships percentage wise there are among newly generated ships
    private static final double COMMERCIAL_TOP_UP_RATIO = 0.90;

    public static SimulationEngine getInstance() {
        return INSTANCE;
    }

    // One session per terminal identified with terminal's ID
    private final Map<Integer, TerminalSession> sessions = new HashMap<>();

    // We don't add new listeners often, and we need concurrent access to the list
    private final List<SimulationListener> listeners = new CopyOnWriteArrayList<>();

    private final AtomicInteger pendingExits = new AtomicInteger(0);
    private final AtomicInteger pendingDocks = new AtomicInteger(0);

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final PortService portService = PortService.getInstance();
    private final ShipGenerator shipGenerator = ShipGenerator.getInstance();

    private volatile boolean running;

    private SimulationEngine() {}

    public void addShip(Ship ship) {
        if (!running) return;

        pendingDocks.incrementAndGet();
        startEntering(ship, portService.getTerminals().getFirst().getIdTerminal());
    }

    public void start(int minShipsPerTerminal) {
        initSessions();
        placeAdditionalShips(minShipsPerTerminal);

        var exiting = selectExitingShips();

        running = true;

        for (var entry: exiting.entrySet()) {
            int idTerminal = entry.getKey();

            for (var ship : entry.getValue())
                startExiting(ship, idTerminal);
        }
    }

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

    public void reportThreadFinished(Goal goal) {
        if (goal == UNDOCK_AND_EXIT) {
            if (pendingExits.decrementAndGet() <= 0 && pendingDocks.get() <= 0)
                finish();
        }
        else {
            if (pendingDocks.decrementAndGet() <= 0 && pendingExits.get() <= 0)
                finish();
        }
    }

    private void initSessions() {
        for (var terminal: portService.getTerminals())
            sessions.put(terminal.getIdTerminal(), new TerminalSession(terminal));
    }

    // List of ships that are selected to exit at each terminal
    private Map<Integer, List<Ship>> selectExitingShips() {
        var result = new HashMap<Integer, List<Ship>>();

        for (var terminal: portService.getTerminals()) {
            var ships = new ArrayList<>(
                    terminal.getShips().stream()
                            .filter(ship -> !(ship instanceof StateShip))
                            .toList());

            Collections.shuffle(ships);

            var count = (int) Math.round(ships.size() * EXIT_SELECTION_RATIO);
            result.put(terminal.getIdTerminal(), ships.subList(0, count));

            pendingExits.addAndGet(count);
        }

        return result;
    }

    private void placeAdditionalShips(int minTerminalShips) {
        for (var terminal: portService.getTerminals()) {
            var newShips = generateAdditionalShips(terminal, minTerminalShips);

            var freeDocks = new ArrayList<>(terminal.getDockPositions());
            freeDocks.removeIf(terminal::isOccupied);
            Collections.shuffle(freeDocks);

            for (var ship: newShips) {
                if (freeDocks.isEmpty()) break;

                var dock = freeDocks.removeLast();

                terminal.registerShip(ship);
                terminal.placeShip(ship, dock);
            }
        }
    }

    private List<Ship> generateAdditionalShips(Terminal terminal, int minTerminalShips) {
        var numCurrentShips = terminal.getShips().size();
        var freeDocks = terminal.getFreeDocks();
        var numShips = Math.clamp(minTerminalShips - numCurrentShips, 0, freeDocks);

        var ships = new ArrayList<Ship>();
        var numCommercialShips = (int) Math.round(COMMERCIAL_TOP_UP_RATIO * numShips);
        var numStateShips = numShips - numCommercialShips;

        for (; numCommercialShips > 0; --numCommercialShips)
            ships.add(shipGenerator.generateCommercialShip());

        for (; numStateShips > 0; --numStateShips)
            ships.add(shipGenerator.generateStateShip());

        return ships;
    }

    private void startEntering(Ship ship, int idTerminal) {
        var session = sessions.get(idTerminal);
        var start = new Position(0, 0);

        var thread = new ShipThread(ship, ENTER_AND_DOCK, session, start);
        executor.submit(thread);
    }

    private void startExiting(Ship ship, int idTerminal) {
        var session = sessions.get(idTerminal);
        var start = session.getTerminal()
                .getShipPosition(ship)
                .orElseThrow(() -> new IllegalStateException(
                        "Specified ship is not part of terminal ID %d".formatted(idTerminal)));

        var thread = new ShipThread(ship, UNDOCK_AND_EXIT, session, start);
        executor.submit(thread);
    }

    private void finish() {
        if (!running) return;
        running = false;

        executor.shutdown();

        notifyListeners(SimulationListener::onSimulationFinished);
    }
}

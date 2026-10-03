package portsim.simulation;

import org.jspecify.annotations.NullMarked;
import portsim.model.Terminal;
import portsim.model.ship.Ship;
import portsim.model.ship.state.StateShip;
import portsim.service.PortService;
import portsim.simulation.event.SimulationListener;
import portsim.simulation.session.TerminalSession;
import portsim.util.ShipGenerator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

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

    private final PortService portService = PortService.getInstance();
    private final ShipGenerator shipGenerator = ShipGenerator.getInstance();

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
}

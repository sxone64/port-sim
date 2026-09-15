package portsim.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import portsim.io.PortPersistence;
import portsim.model.Port;
import portsim.model.Terminal;
import portsim.model.TerminalNotFoundException;
import portsim.model.ship.Ship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

public final class PortService {
    private static final PortService INSTANCE = new PortService();

    public static PortService getInstance() {
        return INSTANCE;
    }

    private final Port port;
    private final Set<Integer> imoRegistry = new HashSet<>();

    private final PortPersistence portPersistence = PortPersistence.getInstance();

    private PortService() {
        port = portPersistence.loadOrCreate();

        port.terminals().stream()
                .flatMap(terminal -> terminal.getShips().stream())
                .map(Ship::getImo)
                .forEach(imoRegistry::add);
    }

    public @NotNull @Unmodifiable List<Ship> getShips(int idTerminal) {
        var terminal = port.getTerminal(idTerminal);
        return terminal.map(value -> List.copyOf(value.getShips())).orElseGet(List::of);
    }

    public int getTotalShips() {
        return getTotalCount(terminal -> terminal.getShips().size());
    }

    public int getTotalStateShips() {
        return getTotalCount(Terminal::getStateShipCount);
    }

    public int getTotalDocks() {
        return getTotalCount(Terminal::getNumDocks);
    }

    public int getTotalFreeDocks() {
        return getTotalCount(Terminal::getFreeDocks);
    }

    public int getFreeDocks(int idTerminal) {
        var terminal = port.getTerminal(idTerminal)
                .orElseThrow(() -> new TerminalNotFoundException(idTerminal));

        return terminal.getFreeDocks();
    }

    public @NotNull @Unmodifiable List<Terminal> getTerminals() {
        return List.copyOf(port.terminals());
    }

    public boolean isImoTaken(int imo) {
        return imoRegistry.contains(imo);
    }

    public void addShip(int idTerminal, @NotNull Ship ship) throws ImoConflictException {
        var terminal = port.getTerminal(idTerminal)
                .orElseThrow(() -> new TerminalNotFoundException(idTerminal));

        if (imoRegistry.contains(ship.getImo()))
            throw new ImoConflictException("Specified ship IMO is already taken");

        imoRegistry.add(ship.getImo());
        terminal.addShip(ship);
        portPersistence.savePort(port);
    }

    private int getTotalCount(ToIntFunction<Terminal> mapper) {
        return port.terminals().stream()
                .mapToInt(mapper)
                .sum();
    }
}

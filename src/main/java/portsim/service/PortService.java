package portsim.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import portsim.io.PortPersistence;
import portsim.model.Port;
import portsim.model.Terminal;
import portsim.model.NotFoundException;
import portsim.model.ship.Ship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

public final class PortService {
    private static final PortService INSTANCE = new PortService();

    private static final String IMO_CONFLICT = "Specified IMO is already taken";

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
        var terminal = getTerminal(idTerminal);
        return List.copyOf(terminal.getShips());
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
        var terminal = getTerminal(idTerminal);
        return terminal.getFreeDocks();
    }

    public @NotNull @Unmodifiable List<Terminal> getTerminals() {
        return List.copyOf(port.terminals());
    }

    public boolean isImoTaken(int imo) {
        return imoRegistry.contains(imo);
    }

    public void addShip(int idTerminal, @NotNull Ship ship) throws ImoConflictException {
        var terminal = getTerminal(idTerminal);

        if (imoRegistry.contains(ship.getImo()))
            throw new ImoConflictException(IMO_CONFLICT);

        imoRegistry.add(ship.getImo());
        terminal.addShip(ship);
        portPersistence.savePort(port);
    }

    public void removeShip(int idTerminal, @NotNull Ship ship) {
        var terminal = getTerminal(idTerminal);

        imoRegistry.remove(ship.getImo());
        terminal.removeShip(ship);
        portPersistence.savePort(port);
    }

    public void updateShip(int idTerminal,
                           @NotNull Ship oldShip,
                           @NotNull Ship newShip) throws ImoConflictException {
        var terminal = getTerminal(idTerminal);

        var newImo = newShip.getImo();
        var isImoUpdated = oldShip.getImo() != newImo;

        if (isImoUpdated && isImoTaken(newImo))
            throw new ImoConflictException(IMO_CONFLICT);

        if (isImoUpdated)
            imoRegistry.add(newImo);

        terminal.updateShip(oldShip, newShip);
        portPersistence.savePort(port);
    }

    private int getTotalCount(ToIntFunction<Terminal> mapper) {
        return port.terminals().stream()
                .mapToInt(mapper)
                .sum();
    }

    private @NotNull Terminal getTerminal(int idTerminal) {
        return port.getTerminal(idTerminal)
                .orElseThrow(() -> new NotFoundException("Terminal with ID %d doesn't exist".formatted(idTerminal)));
    }
}

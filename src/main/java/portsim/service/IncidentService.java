package portsim.service;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.Terminal;
import portsim.model.ship.Ship;
import portsim.simulation.event.Incident;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public final class IncidentService {
    private static final IncidentService INSTANCE = new IncidentService();

    private static final int CHANNEL_LEFT_ROW = 1, CHANNEL_RIGHT_ROW = 2;
    private static final double INCIDENT_PROBABILITY = 0.02;

    public static IncidentService getInstance() {
        return INSTANCE;
    }

    private IncidentService() {}

    /*
        Incidents happen when a moving ship is passing by another ship in the channel
        while overtaking or simply moving in its lane
     */
    public Optional<Incident> checkIncident(Ship causer, Terminal terminal, Position from, Position to) {
        var neighbor = findOccupiedNeighbor(terminal, from, to);

        if (neighbor.isEmpty())
            return Optional.empty();

        if (ThreadLocalRandom.current().nextDouble() < INCIDENT_PROBABILITY) {
            var participant = terminal.getShip(neighbor.get());

            if (participant.isPresent())
                return Optional.of(
                        new Incident(
                                causer, participant.get(), neighbor.get(),
                                terminal.getIdTerminal(), LocalDateTime.now())
                );
        }

        return Optional.empty();
    }

    private Optional<Position> findOccupiedNeighbor(Terminal terminal, Position from, Position to) {
        var row = from.row();

        if (row != CHANNEL_LEFT_ROW && row != CHANNEL_RIGHT_ROW)
            return Optional.empty();

        var otherRow = (row == CHANNEL_LEFT_ROW) ? CHANNEL_RIGHT_ROW : CHANNEL_LEFT_ROW;

        var fromOpposite = new Position(otherRow, from.column());
        if (terminal.isOccupied(fromOpposite))
            return Optional.of(fromOpposite);

        var toOpposite = new Position(otherRow, to.column());
        if (terminal.isOccupied(toOpposite))
            return Optional.of(toOpposite);

        return Optional.empty();
    }
}

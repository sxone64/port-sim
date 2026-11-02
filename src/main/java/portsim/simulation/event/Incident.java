package portsim.simulation.event;

import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.ship.Ship;

import java.time.LocalDateTime;

@NullMarked
public record Incident(Ship causer, Ship participant, Position pos, int idTerminal, LocalDateTime time) {}

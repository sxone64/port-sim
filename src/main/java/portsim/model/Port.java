package portsim.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record Port(List<Terminal> terminals) {
    public @NotNull Optional<Terminal> getTerminal(int idTerminal) {
        return terminals.stream()
                .filter(terminal -> terminal.getIdTerminal() == idTerminal)
                .findFirst();
    }
}

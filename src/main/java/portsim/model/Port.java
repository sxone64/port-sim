package portsim.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public record Port(List<Terminal> terminals) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public @NotNull Optional<Terminal> getTerminal(int idTerminal) {
        return terminals.stream()
                .filter(terminal -> terminal.getIdTerminal() == idTerminal)
                .findFirst();
    }
}

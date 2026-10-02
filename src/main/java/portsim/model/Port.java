package portsim.model;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NullMarked
public record Port(List<Terminal> terminals) implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public Optional<Terminal> getTerminal(int idTerminal) {
        return terminals.stream()
                .filter(terminal -> terminal.getIdTerminal() == idTerminal)
                .findFirst();
    }
}

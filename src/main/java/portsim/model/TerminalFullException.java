package portsim.model;

/*
    In normal circumstances this exception shouldn't be thrown, and it is thrown only when a bug is present
    (e.g. user adding a new ship to a terminal even if it's already full)
 */
public final class TerminalFullException extends RuntimeException {
    public TerminalFullException(int idTerminal) {
        super("Terminal with ID %d is already full".formatted(idTerminal));
    }
}

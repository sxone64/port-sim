package portsim.model;

/*
    Thrown to indicate that a wrong ID is used to fetch a terminal
    which in our case is a bug that needs to be logged rather than a user input error
 */
public final class TerminalNotFoundException extends RuntimeException {
    public TerminalNotFoundException(int idTerminal) {
        super("Terminal with ID %d doesn't exist".formatted(idTerminal));
    }
}

package portsim.model;

/*
    Thrown to indicate that the specified entity couldn't be found during an operation.
    In normal circumstances this exception shouldn't be thrown, and it is thrown only when a bug is present
    (e.g. attempting to fetch a Terminal via nonexistent idTerminal)
 */
public final class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

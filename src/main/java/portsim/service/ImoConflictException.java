package portsim.service;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ImoConflictException extends Exception {
    public ImoConflictException(String message) {
        super(message);
    }
}

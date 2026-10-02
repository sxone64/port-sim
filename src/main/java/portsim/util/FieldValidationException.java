package portsim.util;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class FieldValidationException extends Exception {
    public FieldValidationException(String message) {
        super(message);
    }
}

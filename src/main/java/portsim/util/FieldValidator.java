package portsim.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class FieldValidator {
    private static final FieldValidator INSTANCE = new FieldValidator();

    private static final int IMO_LENGTH = 7;

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static FieldValidator getInstance() {
        return INSTANCE;
    }

    private FieldValidator() {}

    public @NotNull String requireNonBlank(String field, String value) throws ValidationException {
        if (value == null || value.isBlank())
            throw new ValidationException("%s cannot be blank".formatted(field));

        return value;
    }

    public int requirePositiveInt(String field, String value) throws ValidationException {
        var nonBlank = requireNonBlank(field, value);

        int parsedValue;
        try {
            parsedValue = Integer.parseInt(nonBlank);
        } catch (NumberFormatException e) {
            throw new ValidationException("%s must be a valid integer".formatted(field));
        }

        if (parsedValue <= 0)
            throw new ValidationException("%s must be greater than zero".formatted(field));

        return parsedValue;
    }

    public double requirePositiveDouble(String field, String value) throws ValidationException {
        var nonBlank = requireNonBlank(field, value);

        double parsedValue;
        try {
            parsedValue = Double.parseDouble(nonBlank);
        } catch (NumberFormatException e) {
            throw new ValidationException("%s must be a valid decimal number".formatted(field));
        }

        if (parsedValue <= 0)
            throw new ValidationException("%s must be greater than zero".formatted(field));

        return parsedValue;
    }

    public int requireValidImo(String field, String value) throws ValidationException {
        var nonBlank = requireNonBlank(field, value);
        int imo = requirePositiveInt(field, nonBlank);

        if (value.length() != IMO_LENGTH)
            throw new ValidationException("%s must be exactly %d digits long".formatted(field, IMO_LENGTH));

        return imo;
    }

    public @NotNull Path requireNonNull(String field, Path path) throws ValidationException {
        if (path == null)
            throw new ValidationException("%s must be selected.".formatted(field));

        return path;
    }
}

package portsim.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class FieldValidator {
    private static final FieldValidator INSTANCE = new FieldValidator();

    private static final int IMO_LENGTH = 7;
    public static final int MIN_SPEED = 30, MAX_SPEED = 120;

    public static FieldValidator getInstance() {
        return INSTANCE;
    }

    private FieldValidator() {}

    public @NotNull String requireNotBlank(String field, String value) throws FieldValidationException {
        if (value == null || value.isBlank())
            throw new FieldValidationException("%s cannot be blank".formatted(field));

        return value;
    }

    public int requirePositiveInt(String field, String value) throws FieldValidationException {
        var notBlank = requireNotBlank(field, value);

        int parsedValue;
        try {
            parsedValue = Integer.parseInt(notBlank);
        } catch (NumberFormatException e) {
            throw new FieldValidationException("%s must be a valid integer".formatted(field));
        }

        if (parsedValue <= 0)
            throw new FieldValidationException("%s must be greater than zero".formatted(field));

        return parsedValue;
    }

    public double requirePositiveDouble(String field, String value) throws FieldValidationException {
        var notBlank = requireNotBlank(field, value);

        double parsedValue;
        try {
            parsedValue = Double.parseDouble(notBlank);
        } catch (NumberFormatException e) {
            throw new FieldValidationException("%s must be a valid decimal number".formatted(field));
        }

        if (parsedValue <= 0)
            throw new FieldValidationException("%s must be greater than zero".formatted(field));

        return parsedValue;
    }

    public int requireValidImo(String field, String value) throws FieldValidationException {
        var notBlank = requireNotBlank(field, value);
        int imo = requirePositiveInt(field, notBlank);

        if (value.length() != IMO_LENGTH)
            throw new FieldValidationException("%s must be exactly %d digits long".formatted(field, IMO_LENGTH));

        return imo;
    }

    public @NotNull Path requireNotNull(String field, Path path) throws FieldValidationException {
        if (path == null)
            throw new FieldValidationException("%s must be selected.".formatted(field));

        return path;
    }

    public int requireValidSpeed(String field, int speed) throws FieldValidationException {
        if (speed < MIN_SPEED || speed > MAX_SPEED)
            throw new FieldValidationException("%s must be in range [%d, %d]".formatted(field, MIN_SPEED, MAX_SPEED));

        return speed;
    }
}

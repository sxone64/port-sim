package portsim.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

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
        return requirePositiveNumber(
                field,
                value,
                Integer::parseInt,
                "%s must be a valid positive whole number".formatted(field));
    }

    public double requirePositiveDouble(String field, String value) throws FieldValidationException {
        return requirePositiveNumber(
                field,
                value,
                Double::parseDouble,
                "%s must be a valid positive decimal number".formatted(field));
    }

    public int requireValidImo(String field, String value) throws FieldValidationException {
        var notBlank = requireNotBlank(field, value);
        int imo = requirePositiveInt(field, notBlank);

        if (value.length() != IMO_LENGTH)
            throw new FieldValidationException("%s must be exactly %d digits long".formatted(field, IMO_LENGTH));

        return imo;
    }

    public @NotNull Path requireValidPath(String field, Path path) throws FieldValidationException {
        if (path == null || !Files.exists(path))
            throw new FieldValidationException("%s doesn't exist".formatted(field));

        return path;
    }

    public int requireValidSpeed(String field, int speed) throws FieldValidationException {
        if (speed < MIN_SPEED || speed > MAX_SPEED)
            throw new FieldValidationException("%s must be in range [%d, %d]".formatted(field, MIN_SPEED, MAX_SPEED));

        return speed;
    }

    private @NotNull <T extends Number> T requirePositiveNumber(
            String field,
            String value,
            @NotNull Function<String, T> parser,
            String parseFailedMessage) throws FieldValidationException {
        requireNotBlank(field, value);

        try {
            var parsed = parser.apply(value);

            if (parsed.doubleValue() <= 0)
                throw new FieldValidationException("%s must be a positive number".formatted(field));

            return parsed;
        } catch (NumberFormatException e) {
            throw new FieldValidationException(parseFailedMessage);
        }
    }
}

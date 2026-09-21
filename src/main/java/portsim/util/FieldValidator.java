package portsim.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public final class FieldValidator {
    private static final FieldValidator INSTANCE = new FieldValidator();

    public static final int MIN_IMO = 1_000_000, MAX_IMO = 9_999_999;
    public static final int MIN_SPEED = 30, MAX_SPEED = 120;
    public static final int MIN_CAPACITY = 1_000, MAX_CAPACITY = 24_000;
    public static final int MIN_NUM_PASSENGERS = 500, MAX_NUM_PASSENGERS = 7_000;
    public static final int MIN_VOLUME = 200_000, MAX_VOLUME = 3_000_000;

    public static FieldValidator getInstance() {
        return INSTANCE;
    }

    private FieldValidator() {}

    public @NotNull String requireNotBlank(@NotNull String field,
                                           @Nullable String value) throws FieldValidationException {
        if (value == null || value.isBlank())
            throw new FieldValidationException("%s cannot be blank".formatted(field));

        return value;
    }

    public int requirePositiveInt(@NotNull String field,
                                  @Nullable String value) throws FieldValidationException {
        return requirePositiveNumber(
                field,
                value,
                Integer::parseInt,
                "%s must be a valid positive whole number".formatted(field));
    }

    public double requirePositiveDouble(@NotNull String field,
                                        @Nullable String value) throws FieldValidationException {
        return requirePositiveNumber(
                field,
                value,
                Double::parseDouble,
                "%s must be a valid positive decimal number".formatted(field));
    }

    public int requireValidImo(@NotNull String field,
                               @Nullable String value) throws FieldValidationException {
        var notBlank = requireNotBlank(field, value);
        int imo = requirePositiveInt(field, notBlank);

        if (imo < MIN_IMO || imo > MAX_IMO)
            throw new FieldValidationException("%s must be exactly 7 digits long".formatted(field));

        return imo;
    }

    public @NotNull Path requireValidPath(@NotNull String field,
                                          @Nullable Path path) throws FieldValidationException {
        if (path == null || !Files.exists(path))
            throw new FieldValidationException("%s doesn't exist".formatted(field));

        return path;
    }

    public int requireValidSpeed(@NotNull String field,
                                 int speed) throws FieldValidationException {
        return requireInRange(field, speed, MIN_SPEED, MAX_SPEED);
    }

    public int requireValidCapacity(@NotNull String field,
                                    @Nullable String capacity) throws FieldValidationException {
        var parsed = requirePositiveInt(field, capacity);
        return requireInRange(field, parsed, MIN_CAPACITY, MAX_CAPACITY);
    }

    public int requireValidNumPassengers(@NotNull String field,
                                         @Nullable String numPassengers) throws FieldValidationException {
        var parsed = requirePositiveInt(field, numPassengers);
        return requireInRange(field, parsed, MIN_NUM_PASSENGERS, MAX_NUM_PASSENGERS);
    }

    public double requireValidVolume(@NotNull String field,
                                     @Nullable String volume) throws FieldValidationException {
        var parsed = requirePositiveDouble(field, volume);
        return requireInRange(field, parsed, MIN_VOLUME, MAX_VOLUME);
    }

    private @NotNull <T extends Number> T requirePositiveNumber(
            @NotNull String field,
            @Nullable String value,
            @NotNull Function<String, T> parser,
            @NotNull String parseFailedMessage) throws FieldValidationException {
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

    private <T extends Number> @NotNull T requireInRange(
            @NotNull String field,
            @NotNull T value,
            int min, int max) throws FieldValidationException {

        if (value.doubleValue() < min || value.doubleValue() > max)
            throw new FieldValidationException(
                    "%s must be between %,d and %,d".formatted(field, min, max));

        return value;
    }
}

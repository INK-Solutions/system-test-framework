package house.inksoftware.systemtest.domain.steps.response.rest;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
public class BodyCheck {
    private String path;
    private ComparisonType type;
    private Object value;

    public boolean validate(Object actualValue) {
        return type.compare(actualValue, value);
    }

    @Getter
    public enum ComparisonType {
        GREATER_THAN("GREATER_THAN") {
            @Override
            public boolean compare(Object actual, Object expected) {
                // Handle date comparison if expected is a date expression
                if (expected instanceof String && ((String) expected).startsWith("now()")) {
                    return compareDates(actual, expected, (a, b) -> a.isAfter(b));
                }

                // Fall back to number comparison
                return compareNumbers(actual, expected, (a, b) -> a > b);
            }
        };

        private final String value;

        ComparisonType(String value) {
            this.value = value;
        }

        public abstract boolean compare(Object actual, Object expected);

        protected boolean compareNumbers(Object actual, Object expected, BiFunction<Double, Double, Boolean> comparison) {
            try {
                Double actualNum = convertToDouble(actual);
                Double expectedNum = convertToDouble(expected);
                return comparison.apply(actualNum, expectedNum);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        protected boolean compareDates(Object actual, Object expected, BiFunction<LocalDateTime, LocalDateTime, Boolean> comparison) {
            try {
                LocalDateTime actualDate = convertToLocalDateTime(actual);
                LocalDateTime expectedDate = parseTimeExpression((String) expected);
                return comparison.apply(actualDate, expectedDate);
            } catch (Exception e) {
                return false;
            }
        }

        private Double convertToDouble(Object value) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
            throw new NumberFormatException("Cannot convert value to number: " + value);
        }

        private LocalDateTime convertToLocalDateTime(Object value) {
            if (value instanceof LocalDateTime) {
                return (LocalDateTime) value;
            }
            if (value instanceof ZonedDateTime) {
                return ((ZonedDateTime) value).toLocalDateTime();
            }
            if (value instanceof Date) {
                return LocalDateTime.ofInstant(((Date) value).toInstant(), java.time.ZoneId.systemDefault());
            }
            if (value instanceof String) {
                try {
                    // Try common ISO format
                    return LocalDateTime.parse((String) value);
                } catch (DateTimeParseException e) {
                    // Try with formatter that handles formats like "2025-02-26T20:00:34.288782"
                    return LocalDateTime.parse((String) value, DateTimeFormatter.ISO_DATE_TIME);
                }
            }
            throw new IllegalArgumentException("Cannot convert value to LocalDateTime: " + value);
        }

        private LocalDateTime parseTimeExpression(String expression) {
            // Pattern for expressions like "now() + 30min" or "now() - 5h"
            Pattern pattern = Pattern.compile("now\\(\\)\\s*([+-])\\s*(\\d+)\\s*(min|hour|h|day|d|sec|s)");
            Matcher matcher = pattern.matcher(expression.trim());

            if (matcher.find()) {
                String operation = matcher.group(1);
                int amount = Integer.parseInt(matcher.group(2));
                String unit = matcher.group(3);

                LocalDateTime now = LocalDateTime.now();

                ChronoUnit chronoUnit;
                switch (unit.toLowerCase()) {
                    case "min":
                        chronoUnit = ChronoUnit.MINUTES;
                        break;
                    case "hour":
                    case "h":
                        chronoUnit = ChronoUnit.HOURS;
                        break;
                    case "day":
                    case "d":
                        chronoUnit = ChronoUnit.DAYS;
                        break;
                    case "sec":
                    case "s":
                        chronoUnit = ChronoUnit.SECONDS;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported time unit: " + unit);
                }

                return operation.equals("+")
                        ? now.plus(amount, chronoUnit)
                        : now.minus(amount, chronoUnit);
            } else if ("now()".equals(expression.trim())) {
                return LocalDateTime.now();
            }

            throw new IllegalArgumentException("Invalid time expression: " + expression);
        }
    }
}
package house.inksoftware.systemtest.domain.steps.response.rest;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.function.BiFunction;

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

        private Double convertToDouble(Object value) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
            throw new NumberFormatException("Cannot convert value to number: " + value);
        }
    }
}
package studio.humpback.backend.repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

public abstract class AbstractDslQuerySpecification<T> implements DslQuerySpecification<T> {

    private static final String INVALID_DSL = "Invalid dsl filter '%s'. Expected format field:op:value";
    private static final String UNSUPPORTED_DSL_FIELD = "Unsupported dsl field '%s'";
    private static final String UNSUPPORTED_DSL_OPERATION = "Unsupported dsl operation '%s' for field '%s'";
    private static final String INVALID_BOOLEAN_VALUE = "Invalid boolean value '%s' for field '%s'";
    private static final String INVALID_DATE_VALUE = "Invalid date value '%s' for field '%s'. Expected ISO-8601 instant.";
    private static final String INVALID_ENUM_VALUE = "Invalid enum value '%s' for field '%s'";
    private static final String OP_EQ = "eq";
    private static final String OP_NEQ = "neq";
    private static final String OP_GT = "gt";
    private static final String OP_GTE = "gte";
    private static final String OP_LT = "lt";
    private static final String OP_LTE = "lte";
    private static final String OP_IN = "in";
    private static final String OP_CONTAINS = "contains";
    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_FALSE = "false";
    private static final String VALUES_SEPARATOR_REGEX = "\\|";

    private final Class<T> entityClass;
    private final String rootAlias;

    protected AbstractDslQuerySpecification(Class<T> entityClass, String rootAlias) {
        this.entityClass = entityClass;
        this.rootAlias = rootAlias;
    }

    @Override
    public Predicate toPredicate(List<String> dslFilters) {
        PathBuilder<T> root = new PathBuilder<>(entityClass, rootAlias);
        BooleanBuilder builder = new BooleanBuilder();

        for (String token : dslFilters) {
            DslCriterion criterion = parse(token);
            builder.and(buildPredicate(root, criterion));
        }
        return builder;
    }

    protected abstract Predicate buildPredicate(PathBuilder<T> root, DslCriterion criterion);

    protected String normalizeField(String rawField) {
        return rawField;
    }

    protected IllegalArgumentException unsupportedField(String field) {
        return new IllegalArgumentException(String.format(UNSUPPORTED_DSL_FIELD, field));
    }

    protected Predicate datePredicate(PathBuilder<T> root, String field, String op, String rawValue) {
        var path = root.getDateTime(field, Instant.class);
        Instant value = parseInstant(field, rawValue);
        return switch (op) {
            case OP_EQ -> path.eq(value);
            case OP_NEQ -> path.ne(value);
            case OP_GT -> path.gt(value);
            case OP_GTE -> path.goe(value);
            case OP_LT -> path.lt(value);
            case OP_LTE -> path.loe(value);
            case OP_IN -> path.in(parseInstantList(field, rawValue));
            default -> throw unsupportedOperation(op, field);
        };
    }

    protected Predicate booleanPredicate(PathBuilder<T> root, String field, String op, String rawValue) {
        var path = root.getBoolean(field);
        if (!op.equals(OP_EQ) && !op.equals(OP_NEQ) && !op.equals(OP_IN)) {
            throw unsupportedOperation(op, field);
        }
        if (op.equals(OP_IN)) {
            return path.in(parseBooleanList(field, rawValue));
        }
        boolean value = parseBoolean(field, rawValue);
        return op.equals(OP_EQ) ? path.eq(value) : path.ne(value);
    }

    protected <E extends Enum<E>> Predicate enumPredicate(
            PathBuilder<T> root,
            String field,
            String op,
        String rawValue,
            Class<E> enumType) {
        var path = root.getEnum(field, enumType);
        if (!op.equals(OP_EQ) && !op.equals(OP_NEQ) && !op.equals(OP_IN)) {
            throw unsupportedOperation(op, field);
        }
        if (op.equals(OP_IN)) {
            return path.in(parseEnumList(field, rawValue, enumType));
        }
        E value = parseEnumValue(field, rawValue, enumType);
        return op.equals(OP_EQ) ? path.eq(value) : path.ne(value);
    }

    protected Predicate stringPredicate(PathBuilder<T> root, String field, String op, String rawValue) {
        var path = root.getString(field);
        return switch (op) {
            case OP_EQ -> path.eq(rawValue);
            case OP_NEQ -> path.ne(rawValue);
            case OP_CONTAINS -> path.containsIgnoreCase(rawValue);
            case OP_IN -> path.in(splitValues(rawValue));
            default -> throw unsupportedOperation(op, field);
        };
    }

    protected DslCriterion parse(String token) {
        String[] parts = token.split(":", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException(String.format(INVALID_DSL, token));
        }

        String field = normalizeField(parts[0].trim());
        String operation = parts[1].trim().toLowerCase(Locale.ROOT);
        String value = parts[2].trim();

        return new DslCriterion(field, operation, value);
    }

    private IllegalArgumentException unsupportedOperation(String operation, String field) {
        return new IllegalArgumentException(String.format(UNSUPPORTED_DSL_OPERATION, operation, field));
    }

    private Instant parseInstant(String field, String rawValue) {
        try {
            return Instant.parse(rawValue);
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format(INVALID_DATE_VALUE, rawValue, field));
        }
    }

    private List<Instant> parseInstantList(String field, String rawValue) {
        return splitValues(rawValue).stream()
                .map(value -> parseInstant(field, value))
                .toList();
    }

    private boolean parseBoolean(String field, String rawValue) {
        if (!rawValue.equalsIgnoreCase(BOOLEAN_TRUE) && !rawValue.equalsIgnoreCase(BOOLEAN_FALSE)) {
            throw new IllegalArgumentException(String.format(INVALID_BOOLEAN_VALUE, rawValue, field));
        }
        return Boolean.parseBoolean(rawValue);
    }

    private List<Boolean> parseBooleanList(String field, String rawValue) {
        return splitValues(rawValue).stream()
                .map(value -> parseBoolean(field, value))
                .toList();
    }

    protected <E extends Enum<E>> E parseEnumValue(String field, String rawValue, Class<E> enumType) {
        try {
            return Enum.valueOf(enumType, rawValue.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format(INVALID_ENUM_VALUE, rawValue, field));
        }
    }

    private <E extends Enum<E>> List<E> parseEnumList(String field, String rawValue, Class<E> enumType) {
        return splitValues(rawValue).stream()
                .map(value -> parseEnumValue(field, value, enumType))
                .toList();
    }

    private List<String> splitValues(String rawValue) {
        return Arrays.stream(rawValue.split(VALUES_SEPARATOR_REGEX))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    protected record DslCriterion(String field, String operation, String value) {
    }
}

package studio.humpback.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Predicate;

class BookingDslQuerySpecificationTest {

    private final BookingDslQuerySpecification specification = new BookingDslQuerySpecification();

    @Test
    void toPredicateBuildsAndPredicateForMultipleDslTokens() {
        Predicate predicate = specification.toPredicate(List.of(
                "bookingAt:gte:2026-02-20T00:00:00Z",
                "hasBeenPayed:eq:false",
                "name:contains:alice"));

        String expression = predicate.toString();
        assertThat(expression).contains("bookingAt");
        assertThat(expression).contains("hasBeenPayed");
        assertThat(expression).contains("name");
    }

    @Test
    void toPredicateAcceptsAliasFieldsAndOperationCase() {
        Predicate predicate = specification.toPredicate(List.of(
                "payment:EQ:true",
                "type:eq:RECORDING"));

        String expression = predicate.toString();
        assertThat(expression).contains("hasBeenPayed");
        assertThat(expression).contains("bookingType");
    }

    @Test
    void toPredicateRejectsInvalidDslShape() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> specification.toPredicate(List.of("invalid")));

        assertThat(ex.getMessage()).contains("Expected format field:op:value");
    }

    @Test
    void toPredicateRejectsUnsupportedField() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> specification.toPredicate(List.of("unknown:eq:value")));

        assertThat(ex.getMessage()).contains("Unsupported dsl field 'unknown'");
    }

    @Test
    void toPredicateRejectsUnsupportedOperationForBooleanField() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> specification.toPredicate(List.of("hasBeenPayed:gte:true")));

        assertThat(ex.getMessage()).contains("Unsupported dsl operation 'gte' for field 'hasBeenPayed'");
    }

    @Test
    void toPredicateRejectsInvalidBooleanValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> specification.toPredicate(List.of("hasBeenPayed:eq:yes")));

        assertThat(ex.getMessage()).contains("Invalid boolean value 'yes' for field 'hasBeenPayed'");
    }

    @Test
    void toPredicateRejectsInvalidInstantValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> specification.toPredicate(List.of("bookingAt:gte:not-a-date")));

        assertThat(ex.getMessage()).contains("Invalid date value 'not-a-date' for field 'bookingAt'");
    }
}

package studio.humpback.backend.repository;

import org.springframework.stereotype.Component;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import studio.humpback.backend.model.Booking;
import studio.humpback.backend.model.BookingType;

@Component
public class BookingDslQuerySpecification extends AbstractDslQuerySpecification<Booking> {

    public BookingDslQuerySpecification() {
        super(Booking.class, "booking");
    }

    @Override
    protected Predicate buildPredicate(PathBuilder<Booking> booking, DslCriterion criterion) {
        String field = criterion.field();
        String op = criterion.operation();
        String value = criterion.value();

        return switch (field) {
            case "bookingAt", "endAt" -> datePredicate(booking, field, op, value);
            case "hasBeenPayed" -> booleanPredicate(booking, field, op, value);
            case "bookingType" -> enumPredicate(booking, field, op, value, BookingType.class);
            case "id", "name", "email", "phone" -> stringPredicate(booking, field, op, value);
            default -> throw unsupportedField(field);
        };
    }

    @Override
    protected String normalizeField(String rawField) {
        return switch (rawField) {
            case "payment", "paid" -> "hasBeenPayed";
            case "type" -> "bookingType";
            default -> rawField;
        };
    }

    @Override
    protected <E extends Enum<E>> E parseEnumValue(String field, String rawValue, Class<E> enumType) {
        if ("bookingType".equals(field) && enumType.equals(BookingType.class)) {
            return enumType.cast(BookingType.fromApiValue(rawValue));
        }
        return super.parseEnumValue(field, rawValue, enumType);
    }
}

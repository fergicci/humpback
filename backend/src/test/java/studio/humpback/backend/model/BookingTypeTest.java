package studio.humpback.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BookingTypeTest {

    @Test
    void fromApiValueAcceptsSpacedHyphenatedAndLowercaseValues() {
        assertThat(BookingType.fromApiValue("Rehearsal Recording")).isEqualTo(BookingType.REHARSAL_RECORDING);
        assertThat(BookingType.fromApiValue("video-production")).isEqualTo(BookingType.VIDEO_PRODUCTION);
        assertThat(BookingType.fromApiValue("mixing")).isEqualTo(BookingType.MIXING);
    }

    @Test
    void fromApiValueRejectsInvalidValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BookingType.fromApiValue("Unknown type"));

        assertThat(ex.getMessage()).contains("Unsupported booking type");
    }
}

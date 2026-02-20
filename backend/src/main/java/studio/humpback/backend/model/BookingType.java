package studio.humpback.backend.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum BookingType {

    REHARSAL("booking.type.rehearsal", Collections.singleton(BookingRoom.LIVE_ROOM)),
    REHARSAL_RECORDING("booking.type.rehearsalAndRecording", 
        Set.of(BookingRoom.LIVE_ROOM, BookingRoom.ENGINEERING_ROOM)),
    RECORDING("booking.type.recording", 
        Set.of(BookingRoom.LIVE_ROOM, BookingRoom.ENGINEERING_ROOM)),
    MIXING("booking.type.mixing",
        Collections.singleton(BookingRoom.ENGINEERING_ROOM)),
    MASTERING("booking.type.mastering",
        Collections.singleton(BookingRoom.ENGINEERING_ROOM)),
    VIDEO_PRODUCTION("booking.type.videoProduction",
        Set.of(BookingRoom.LIVE_ROOM, BookingRoom.ENGINEERING_ROOM));

    private final String label;
    private final Set<BookingRoom> usedRooms;

    private BookingType(String label, Set<BookingRoom> usedRooms) {
        this.label = label;
        this.usedRooms = usedRooms;
    }
    
    public String getLabel() {
        return label;
    }

    public Set<BookingRoom> getUsedRooms() {
        return usedRooms;
    }

    public static List<BookingType> allowOverlap() {
        return List.of(BookingType.values())
            .stream()
            .filter(type -> type.getUsedRooms().size() == 1)
            .toList();
    }

    public static BookingType fromApiValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Booking type is required");
        }

        String normalized = normalize(rawValue);
        return Arrays.stream(BookingType.values())
                .filter(type -> normalize(type.name()).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported booking type '%s'", rawValue)));
    }

    public String toDisplayLabel() {
        return Arrays.stream(name().split("_"))
                .map(word -> word.substring(0, 1) + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    private static String normalize(String rawValue) {
        String normalized = rawValue
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_')
                .replaceAll("[^A-Z0-9_]", "_")
                .replaceAll("_+", "_");

        // Keep backward compatibility with the existing enum typo: REHARSAL.
        normalized = normalized.replace("REHEARSAL", "REHARSAL");

        if (normalized.startsWith("_")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}

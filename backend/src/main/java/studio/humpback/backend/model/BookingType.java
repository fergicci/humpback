package studio.humpback.backend.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
}

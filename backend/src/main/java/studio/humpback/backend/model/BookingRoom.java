package studio.humpback.backend.model;

public enum BookingRoom {
    LIVE_ROOM("booking.room.liveRoom"),
    ENGINEERING_ROOM("booking.room.engineeringRoom");

    private final String label;

    private BookingRoom(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package jumdo12.springgomok.presentation.dto;

public record RoomUpdateEvent(
        String type,
        String userName,
        Object data
) {
}

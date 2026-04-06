package jumdo12.springgomok.application.event;

import jumdo12.springgomok.domain.GomokRoom;

public record RoomUpdatedEvent(Long roomId, GomokRoom room) {
}

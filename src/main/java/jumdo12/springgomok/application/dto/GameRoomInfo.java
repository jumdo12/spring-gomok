package jumdo12.springgomok.application.dto;

import jumdo12.springgomok.domain.GomokRoom;

public record GameRoomInfo(
        Long roomId,
        String roomName,
        int participantCount
) {

    public static GameRoomInfo from(GomokRoom gomokRoom) {
        return new GameRoomInfo(
                gomokRoom.getId(),
                gomokRoom.getRoomName(),
                gomokRoom.getParticipantCount()
        );
    }
}

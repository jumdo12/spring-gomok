package jumdo12.springgomok.application.dto;

import jumdo12.springgomok.domain.GomokRoom;

public record GameRoomDetailInfo(
        Long roomId,
        String roomName,
        Long myId,
        Long opponentId
) {
    public static GameRoomDetailInfo from(GomokRoom room, Long currentUserId) {
        boolean isHost = room.getHost().getId().equals(currentUserId);

        Long opponentId = isHost
                ? room.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null)
                : room.getHost().getId();

        return new GameRoomDetailInfo(
                room.getId(),
                room.getRoomName(),
                currentUserId,
                opponentId
        );
    }
}

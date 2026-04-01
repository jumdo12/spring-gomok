package jumdo12.springgomok.application.dto;

import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.domain.Player;

public record GameRoomDetailInfo(
        Long roomId,
        String roomName,
        Long myId,
        String myStone,
        boolean isHost,
        String gameStatus,
        Long opponentId,
        String opponentName
) {
    public static GameRoomDetailInfo from(GomokRoom room, Long currentUserId) {
        boolean isHost = room.getHost().getId().equals(currentUserId);

        // 내 돌 색깔 찾기
        Player myPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("참가자를 찾을 수 없습니다."));

        String myStone = myPlayer.getStone().name();

        // 상대방 정보 찾기
        Player opponentPlayer = room.getPlayers().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        Long opponentId = opponentPlayer != null ? opponentPlayer.getUser().getId() : null;
        String opponentName = opponentPlayer != null ? opponentPlayer.getUser().getNickname() : null;

        return new GameRoomDetailInfo(
                room.getId(),
                room.getRoomName(),
                currentUserId,
                myStone,
                isHost,
                room.getGomokRoomStatus().name(),
                opponentId,
                opponentName
        );
    }
}

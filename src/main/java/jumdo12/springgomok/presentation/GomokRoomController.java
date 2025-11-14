package jumdo12.springgomok.presentation;

import jumdo12.springgomok.application.GomokRoomService;
import jumdo12.springgomok.application.UserService;
import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.application.dto.GameRoomInfo;
import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.domain.User;
import jumdo12.springgomok.infra.sse.SseEmitters;
import jumdo12.springgomok.presentation.dto.RoomCreateRequest;
import jumdo12.springgomok.presentation.dto.RoomUpdateEvent;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class GomokRoomController {

    private final GomokRoomService gomokRoomService;
    private final SseEmitters sseEmitters;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<GameRoomInfo> createRoom(
            @RequestBody RoomCreateRequest roomCreateRequest,
            @AuthUser LoginUser loginUser) {
        GomokRoom room = gomokRoomService.createRoom(roomCreateRequest.roomName(), loginUser);

        GameRoomInfo roomInfo = GameRoomInfo.from(room);

        return ResponseEntity.ok(roomInfo);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<GameRoomDetailInfo> getRoomInfo(
            @PathVariable Long roomId,
            @AuthUser LoginUser loginUser) {
        GameRoomDetailInfo gameDetailInfo = gomokRoomService.getGameDetailInfo(roomId, loginUser);
        return ResponseEntity.ok(gameDetailInfo);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<GameRoomInfo>> getWaitingRooms() {
        return ResponseEntity.ok(gomokRoomService.getWaitingRooms());
    }

    @GetMapping("/{roomId}/subscribe")
    public SseEmitter subscribe(
            @PathVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        return sseEmitters.add(roomId, loginUser.id());
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<GameRoomDetailInfo> joinRoom(
            @PathVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        gomokRoomService.joinRoom(roomId, loginUser);
        GameRoomDetailInfo roomInfo = gomokRoomService.getGameDetailInfo(roomId, loginUser);

        sendRoomUpdateEvent(roomId, roomInfo.opponentId(), "PARTICIPANT_JOINED", loginUser.id());

        return ResponseEntity.ok(roomInfo);
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        GameRoomDetailInfo roomInfo = gomokRoomService.getGameDetailInfo(roomId, loginUser);
        gomokRoomService.leaveRoom(roomId, loginUser);

        sendRoomUpdateEvent(roomId, roomInfo.opponentId(), "PARTICIPANT_LEFT", loginUser.id());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/start")
    public ResponseEntity<Void> startGame(
            @PathVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        gomokRoomService.startGame(roomId, loginUser);
        GameRoomDetailInfo roomInfo = gomokRoomService.getGameDetailInfo(roomId, loginUser);

        sseEmitters.sendRoomUpdate(roomId, roomInfo.opponentId(),
                new RoomUpdateEvent("GAME_STARTED", null));

        return ResponseEntity.noContent().build();
    }

    private void sendRoomUpdateEvent(Long roomId, Long opponentId, String eventType, Long userId) {
        if (opponentId == null) {
            return;
        }

        User user = userService.findUser(userId);
        RoomUpdateEvent event = new RoomUpdateEvent(eventType, user.getNickname());
        sseEmitters.sendRoomUpdate(roomId, opponentId, event);
    }
}

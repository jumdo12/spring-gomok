package jumdo12.springgomok.presentation;

import jumdo12.springgomok.application.GomokRoomService;
import jumdo12.springgomok.application.GomokService;
import jumdo12.springgomok.application.UserService;
import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.application.dto.GameRoomInfo;
import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.presentation.dto.RoomCreateRequest;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class GomokRoomController {

    private final GomokRoomService gomokRoomService;

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
}

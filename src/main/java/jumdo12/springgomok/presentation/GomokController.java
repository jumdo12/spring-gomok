package jumdo12.springgomok.presentation;

import jumdo12.springgomok.application.GomokRoomService;
import jumdo12.springgomok.application.GomokService;
import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.infra.sse.SseEmitters;
import jumdo12.springgomok.presentation.dto.MoveEvent;
import jumdo12.springgomok.presentation.dto.PlaceRequest;
import jumdo12.springgomok.presentation.dto.PlaceResponse;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GomokController {

    private final GomokService gomokService;
    private final GomokRoomService gomokRoomService;
    private final SseEmitters sseEmitters;

    @PostMapping("/{roomId}/place")
    public ResponseEntity<PlaceResponse> placeGomok(
            @PathVariable Long roomId,
            @RequestBody PlaceRequest request,
            @AuthUser LoginUser loginUser
    ) {
        gomokService.placeGomok(roomId, loginUser, request.row(), request.col());

        GameRoomDetailInfo roomInfo = gomokRoomService.getGameDetailInfo(roomId, loginUser);
        GomokRoom room = gomokRoomService.findRoom(roomId);

        MoveEvent event = new MoveEvent(
                request.row(),
                request.col(),
                room.getGomokRoomStatus().name(),
                room.getWinner()
        );
        sseEmitters.sendMove(roomId, roomInfo.opponentId(), event);

        return ResponseEntity.ok(new PlaceResponse(
                room.getGomokRoomStatus().name(),
                room.getWinner()
        ));
    }
}

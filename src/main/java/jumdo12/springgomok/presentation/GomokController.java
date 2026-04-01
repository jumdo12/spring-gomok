package jumdo12.springgomok.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jumdo12.springgomok.application.GomokRoomService;
import jumdo12.springgomok.application.GomokService;
import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.domain.GomokRoomStatus;
import jumdo12.springgomok.domain.Stone;
import jumdo12.springgomok.infra.sse.SseEmitters;
import jumdo12.springgomok.presentation.dto.MoveEvent;
import jumdo12.springgomok.presentation.dto.PlaceRequest;
import jumdo12.springgomok.presentation.dto.PlaceResponse;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "오목 게임", description = "오목 게임 진행 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GomokController {

    private final GomokService gomokService;
    private final GomokRoomService gomokRoomService;
    private final SseEmitters sseEmitters;

    @Operation(summary = "돌 놓기")
    @PostMapping("/{roomId}/place")
    public ResponseEntity<PlaceResponse> placeGomok(
            @PathVariable Long roomId,
            @RequestBody PlaceRequest request,
            @AuthUser LoginUser loginUser
    ) {
        gomokService.placeGomok(roomId, loginUser, request.row(), request.col());

        GameRoomDetailInfo roomInfo = gomokRoomService.getGameDetailInfo(roomId, loginUser);
        GomokRoom room = gomokRoomService.findRoom(roomId);

        Stone winnerStone = room.getWinner() != null ? room.getWinner().getStone() : null;

        MoveEvent event = new MoveEvent(
                request.row(),
                request.col(),
                room.getGomokRoomStatus().name(),
                winnerStone
        );
        sseEmitters.sendMove(roomId, roomInfo.opponentId(), event);

        if (room.getGomokRoomStatus() == GomokRoomStatus.FINISHED) {
            sseEmitters.completeRoom(roomId, loginUser.id(), roomInfo.opponentId());
        }

        return ResponseEntity.ok(new PlaceResponse(
                room.getGomokRoomStatus().name(),
                winnerStone
        ));
    }
}

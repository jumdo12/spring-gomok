package jumdo12.springgomok.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jumdo12.springgomok.application.GomokService;
import jumdo12.springgomok.domain.Position;
import jumdo12.springgomok.presentation.dto.PlaceRequest;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GomokStompController {

    private static final String ROOM_DESTINATION = "/sub/room/";

    private final GomokService gomokService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/place")
    public void placeGomok(
            @DestinationVariable Long roomId,
            @AuthUser LoginUser loginUser,
            @Payload PlaceRequest request
    ) {
        Position position = gomokService.placeGomok(roomId, loginUser, request.row(), request.col());
        messagingTemplate.convertAndSend(ROOM_DESTINATION + roomId, position);
    }
}

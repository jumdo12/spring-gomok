package jumdo12.springgomok.presentation;

import jumdo12.springgomok.application.GomokRoomService;
import jumdo12.springgomok.application.UserService;
import jumdo12.springgomok.application.dto.ChatMessage;
import jumdo12.springgomok.infra.stomp.StompDestination;
import jumdo12.springgomok.presentation.dto.*;
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
public class GomokRoomStompController {

    private final GomokRoomService gomokRoomService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/join")
    public void joinRoom(
            @DestinationVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        gomokRoomService.joinRoom(roomId, loginUser);
    }

    @MessageMapping("/room/{roomId}/leave")
    public void leaveRoom(
            @DestinationVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        gomokRoomService.leaveRoom(roomId, loginUser);
    }

    @MessageMapping("/room/{roomId}/start")
    public void startGame(
            @DestinationVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        gomokRoomService.startGame(roomId, loginUser);
        sendToRoom(roomId, new GameStartedEvent());
    }

    @MessageMapping("/room/{roomId}/switch-stone")
    public void switchStone(
            @DestinationVariable Long roomId,
            @AuthUser LoginUser loginUser
    ) {
        StoneSwitchedEvent event = gomokRoomService.switchStone(roomId, loginUser);
        sendToRoom(roomId, event);
    }

    @MessageMapping("/room/{roomId}/chat")
    public void sendChat(
            @DestinationVariable Long roomId,
            @AuthUser LoginUser loginUser,
            @Payload ChatRequest chatRequest
    ) {
        ChatMessage chatMessage = gomokRoomService.sendChatMessage(roomId, loginUser, chatRequest);

        sendToRoom(roomId, new ChatMessageEvent(chatMessage.from(), chatMessage.content()));
    }

    private void sendToRoom(Long roomId, Object payload) {
        messagingTemplate.convertAndSend(StompDestination.ROOM + roomId, payload);
    }
}

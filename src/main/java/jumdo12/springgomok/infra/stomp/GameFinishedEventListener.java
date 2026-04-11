package jumdo12.springgomok.infra.stomp;

import jumdo12.springgomok.application.event.GameFinishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameFinishedEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handle(GameFinishedEvent event) {
        messagingTemplate.convertAndSend(StompDestination.ROOM + event.roomId(), event);
    }
}

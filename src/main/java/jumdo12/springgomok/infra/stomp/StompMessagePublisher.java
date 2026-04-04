package jumdo12.springgomok.infra.stomp;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompMessagePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long roomId, Object data) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, data);
    }
}

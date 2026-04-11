package jumdo12.springgomok.infra.stomp;

import jumdo12.springgomok.application.event.RoomUpdatedEvent;
import jumdo12.springgomok.domain.Player;
import jumdo12.springgomok.presentation.dto.ParticipantInfo;
import jumdo12.springgomok.presentation.dto.RoomParticipantsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomUpdatedEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handle(RoomUpdatedEvent event) {
        List<ParticipantInfo> participants = event.room().getPlayers().stream()
                .map(p -> new ParticipantInfo(
                        p.getUser().getId(),
                        p.getUser().getNickname(),
                        p.getStone().name()))
                .toList();

        RoomParticipantsEvent payload = new RoomParticipantsEvent(participants, event.room().getHost().getId());
        messagingTemplate.convertAndSend(StompDestination.ROOM + event.roomId(), payload);
    }
}

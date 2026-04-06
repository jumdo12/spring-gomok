package jumdo12.springgomok.presentation.dto;

import java.util.List;

public record RoomParticipantsEvent(List<ParticipantInfo> participants, Long hostId) {
}

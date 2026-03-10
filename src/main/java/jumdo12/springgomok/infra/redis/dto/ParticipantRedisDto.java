package jumdo12.springgomok.infra.redis.dto;

import jumdo12.springgomok.domain.Participant;
import jumdo12.springgomok.domain.Stone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRedisDto {

    private Long userId;
    private Stone stone;

    public static ParticipantRedisDto from(Participant participant) {
        return new ParticipantRedisDto(participant.getUser().getId(), participant.getStone());
    }
}

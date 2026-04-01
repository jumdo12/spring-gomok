package jumdo12.springgomok.infra.redis.dto;

import jumdo12.springgomok.domain.Player;
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

    public static ParticipantRedisDto from(Player player) {
        return new ParticipantRedisDto(player.getUser().getId(), player.getStone());
    }
}

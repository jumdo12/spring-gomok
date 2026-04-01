package jumdo12.springgomok.infra.redis.dto;

import jumdo12.springgomok.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GomokRoomRedisDto {

    private Long id;
    private String roomName;
    private GomokRoomStatus gomokRoomStatus;
    private Long hostId;
    private Set<ParticipantRedisDto> participants;
    private GomokRedisDto gomok;

    public static GomokRoomRedisDto from(GomokRoom room) {
        return new GomokRoomRedisDto(
                room.getId(),
                room.getRoomName(),
                room.getGomokRoomStatus(),
                room.getHost().getId(),
                room.getPlayers().stream()
                        .map(ParticipantRedisDto::from)
                        .collect(Collectors.toSet()),
                GomokRedisDto.from(room.getGomok())
        );
    }
}
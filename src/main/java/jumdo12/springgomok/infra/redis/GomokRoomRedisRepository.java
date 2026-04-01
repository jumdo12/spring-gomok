package jumdo12.springgomok.infra.redis;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.infra.redis.dto.GomokRedisDto;
import jumdo12.springgomok.infra.redis.dto.GomokRoomRedisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GomokRoomRedisRepository {

    private static final String KEY_PREFIX = "room:";
    private static final String COUNTER_KEY = "room:counter";
    private static final Duration TTL = Duration.ofHours(3);

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public GomokRoom create(GomokRoom room) {
        Long id = redisTemplate.opsForValue().increment(COUNTER_KEY);
        GomokRoom roomWithId = GomokRoom.restore(
                id,
                room.getRoomName(),
                room.getGomokRoomStatus(),
                room.getHost(),
                room.getPlayers(),
                room.getGomok());
        redisTemplate.opsForValue().set(key(id), GomokRoomRedisDto.from(roomWithId), TTL);
        return roomWithId;
    }

    public void update(GomokRoom room) {
        redisTemplate.opsForValue().set(key(room.getId()), GomokRoomRedisDto.from(room), TTL);
    }

    public Optional<GomokRoom> findById(Long id) {
        GomokRoomRedisDto dto = (GomokRoomRedisDto) redisTemplate.opsForValue().get(key(id));

        if (dto == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(dto));
    }

    public List<GomokRoom> findAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .filter(k -> !k.equals(COUNTER_KEY))
                .map(k -> (GomokRoomRedisDto) redisTemplate.opsForValue().get(k))
                .filter(dto -> dto != null)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        redisTemplate.delete(key(id));
    }

    private GomokRoom toDomain(GomokRoomRedisDto dto) {
        User host = userRepository.findById(dto.getHostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<Player> players = dto.getParticipants().stream()
                .map(p -> {
                    User user = userRepository.findById(p.getUserId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    return new Player(user, p.getStone());
                })
                .collect(Collectors.toSet());

        Gomok gomok = restoreGomok(dto.getGomok());

        return GomokRoom.restore(dto.getId(), dto.getRoomName(), dto.getGomokRoomStatus(), host, players, gomok);
    }

    private Gomok restoreGomok(GomokRedisDto dto) {
        Stone[][] grid = new Stone[dto.getGrid().length][dto.getGrid()[0].length];

        for (int i = 0; i < dto.getGrid().length; i++) {
            for (int j = 0; j < dto.getGrid()[i].length; j++) {
                grid[i][j] = Stone.valueOf(dto.getGrid()[i][j]);
            }
        }

        return Gomok.restore(dto.getId(), grid, dto.getCurrTurn(), dto.getWinner());
    }

    private String key(Long id) {
        return KEY_PREFIX + id;
    }
}

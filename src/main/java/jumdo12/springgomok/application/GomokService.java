package jumdo12.springgomok.application;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.infra.redis.GomokRoomRedisRepository;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GomokService {

    private final GomokRoomRedisRepository gomokRoomRepository;
    private final UserRepository userRepository;
    private final GomokHistoryService gomokHistoryService;

    public void placeGomok(Long roomId, LoginUser loginUser, int row, int col) {
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());

        Stone stone = gomokRoom.placeGomokStone(row, col, user);
        gomokRoomRepository.update(gomokRoom);

        gomokHistoryService.placeGomokHistory(gomokRoom, row, col, stone);
    }

    public void switchStone(Long roomId, LoginUser loginUser) {
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());

        if (!gomokRoom.isHost(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (gomokRoom.getGomokRoomStatus() != GomokRoomStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        gomokRoom.switchParticipantsStone();
        gomokRoomRepository.update(gomokRoom);
    }

    private GomokRoom findRoom(Long roomId) {
        return gomokRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}

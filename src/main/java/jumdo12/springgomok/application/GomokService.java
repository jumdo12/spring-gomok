package jumdo12.springgomok.application;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GomokService {

    private final GomokRoomRepository gomokRoomRepository;
    private final UserRepository userRepository;
    private final GomokHistoryService gomokHistoryService;

    public Position placeGomok(Long roomId, LoginUser loginUser, int row, int col) {
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());

        MoveResult moveResult = gomokRoom.placeGomokStone(new Position(row, col), user);
        gomokRoomRepository.update(gomokRoom);

        if (moveResult.isWinningMove()) {
            gomokHistoryService.saveGomokHistory(gomokRoom);
        }

        return new Position(row, col);
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

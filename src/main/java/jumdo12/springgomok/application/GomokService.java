package jumdo12.springgomok.application;

import jumdo12.springgomok.application.event.GameFinishedEvent;
import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GomokService {

    private final GomokRoomRepository gomokRoomRepository;
    private final UserRepository userRepository;
    private final GomokHistoryService gomokHistoryService;
    private final ApplicationEventPublisher eventPublisher;

    public Position placeGomok(Long roomId, LoginUser loginUser, Position position) {
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());
        Player player = gomokRoom.findPlayer(user);

        MoveResult moveResult = gomokRoom.placeGomokStone(position, player);
        gomokRoomRepository.update(gomokRoom);

        if (moveResult.isWinningMove()) {
            gomokHistoryService.saveGomokHistory(gomokRoom);
            eventPublisher.publishEvent(new GameFinishedEvent(
                    roomId,
                    gomokRoom.getWinner().getUser().getNickname(),
                    gomokRoom.getWinner().getStone()
            ));
        }

        return position;
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

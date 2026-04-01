package jumdo12.springgomok.application;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.dto.GameHistoryResponse;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GomokHistoryService {

    private final GomokHistoryRepository gomokHistoryRepository;
    private final UserRepository userRepository;

    @Async("historyExecutor")
    @Transactional
    public void saveGomokHistory(GomokRoom gomokRoom) {
        Set<Player> players = gomokRoom.getPlayers();

        User whiteUser = players.stream()
                .filter(p -> p.getStone() == Stone.WHITE)
                .map(Player::getUser)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("백돌 플레이어가 없습니다."));

        User blackUser = players.stream()
                .filter(p -> p.getStone() == Stone.BLACK)
                .map(Player::getUser)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("흑돌 플레이어가 없습니다."));

        GomokHistory history = GomokHistory.create(
                gomokRoom.getGomokGameId(),
                LocalDateTime.now(),
                whiteUser,
                blackUser);

        gomokRoom.getGomok().getMoveHistory()
                .forEach(r -> history.addPlaceResult(r.position(), r.stone()));

        history.finishGame(gomokRoom.getWinner().getStone());

        gomokHistoryRepository.save(history);
    }

    public List<GameHistoryResponse> getUserGomokRecordId(LoginUser loginUser) {
        User user = findUser(loginUser.id());

        List<GomokHistory> histories = gomokHistoryRepository
                .findByBlackStoneUserOrWhiteStoneUser(user, user);

        return histories.stream()
                .map(history -> new GameHistoryResponse(history.getGomokId(), history.getStartTime()))
                .collect(Collectors.toList());
    }

    public List<PlaceResult> getGomokPlaceResults(String gomokId) {
        GomokHistory gomokHistory = gomokHistoryRepository.findByGomokId(gomokId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_RESULT_NOT_FOUND));

        return gomokHistory.getPlaceResults();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}

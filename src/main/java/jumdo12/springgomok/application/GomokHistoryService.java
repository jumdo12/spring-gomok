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
    public void createGomokHistory(
            GomokRoom gomokRoom
    ) {
        Set<Participant> participants = gomokRoom.getParticipants();

        User whiteUser = participants.stream()
                .filter(p -> p.getStone() == Stone.WHITE)
                .map(Participant::getUser)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("백돌 플레이어가 없습니다."));

        User blackUser = participants.stream()
                .filter(p -> p.getStone() == Stone.BLACK)
                .map(Participant::getUser)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("흑돌 플레이어가 없습니다."));

        GomokHistory gomokHistory = GomokHistory.create(
                gomokRoom.getGomokGameId(),
                LocalDateTime.now(),
                whiteUser,
                blackUser);

        gomokHistoryRepository.save(gomokHistory);
    }

    @Async("historyExecutor")
    @Transactional
    public void placeGomokHistory(GomokRoom gomokRoom, int row, int col, Stone stone) {
        String gomokGameId = gomokRoom.getGomokGameId();
        GomokHistory gomokHistory = gomokHistoryRepository.getGomokHistoryByGomokId(gomokGameId)
                .orElseThrow(() -> new IllegalStateException("기록을 찾을 수 없습니다."));
        gomokHistory.addPlaceResult(row, col, stone);

        gomokHistoryRepository.save(gomokHistory);
    }

    public List<GameHistoryResponse> getUserGomokRecordId(LoginUser loginUser) {
        User user = findUser(loginUser.id());

        List<GomokHistory> byBlackStoneUserOrWhiteStoneUser = gomokHistoryRepository
                .findByBlackStoneUserOrWhiteStoneUser(user, user);

        return byBlackStoneUserOrWhiteStoneUser.stream()
                .map(history -> new GameHistoryResponse(history.getGomokId(), history.getStartTime()))
                .collect(Collectors.toList());
    }

    public List<PlaceResult> getGomokPlaceResults(String gomokId) {
        GomokHistory gomokHistory = gomokHistoryRepository.findByGomokId(gomokId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_RESULT_NOT_FOUND));

        return gomokHistory.getPlaceResults();
    }

    private User findUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}

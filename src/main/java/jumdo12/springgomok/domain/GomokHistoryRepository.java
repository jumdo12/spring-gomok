package jumdo12.springgomok.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GomokHistoryRepository extends JpaRepository<GomokHistory, Long> {
    Optional<GomokHistory> getGomokHistoryByGomokId(String gomokId);

    List<GomokHistory> findByBlackStoneUserOrWhiteStoneUser(User blackStoneUser, User whiteStoneUser);

    Optional<GomokHistory> findByGomokId(String gomokId);
}

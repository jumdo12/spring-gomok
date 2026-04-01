package jumdo12.springgomok.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@RequiredArgsConstructor
@Getter
public class GomokHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gomokId;

    private LocalDateTime startTime;

    private Long placeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_stone_id")
    private User whiteStoneUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_stone_id")
    private User blackStoneUser;

    @Enumerated(EnumType.STRING)
    private Stone winner;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "gomok_history_id")
    private List<PlaceResult> placeResults;

    private GomokHistory(
            String gomokId,
            LocalDateTime startTime,
            Long placeCount,
            User whiteStoneUser,
            User blackStoneUser,
            List<PlaceResult> placeResults) {
        this.gomokId = gomokId;
        this.startTime = startTime;
        this.placeCount = placeCount;
        this.whiteStoneUser = whiteStoneUser;
        this.blackStoneUser = blackStoneUser;
        this.placeResults = placeResults;
    }

    public static GomokHistory create(String gomokId, LocalDateTime startTime, User whiteStoneUser, User blackStoneUser) {
        return new GomokHistory(
                gomokId,
                startTime,
                0L,
                whiteStoneUser,
                blackStoneUser,
                new ArrayList<>()
        );
    }

    public void finishGame(Stone winner) {
        this.winner = winner;
    }

    public void addPlaceResult(Position position, Stone stone) {
        placeResults.add(new PlaceResult(position.row(), position.col(), stone, ++placeCount));
    }
}

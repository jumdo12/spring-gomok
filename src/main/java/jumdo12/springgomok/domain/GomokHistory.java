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
    private Stone gameResult;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "gomok_place_history",
            joinColumns = @JoinColumn(name = "gomok_history_id")
    )
    private List<PlaceResult> placeResults;

    private GomokHistory(
            String gomokId,
            LocalDateTime startTime,
            Long placeCount,
            User whiteStoneUser,
            User blackStoneUser,
            Stone gameResult,
            List<PlaceResult> placeResults) {
        this.gomokId = gomokId;
        this.startTime = startTime;
        this.placeCount = placeCount;
        this.whiteStoneUser = whiteStoneUser;
        this.blackStoneUser = blackStoneUser;
        this.gameResult = gameResult;
        this.placeResults = placeResults;
    }

    public static GomokHistory create (String gomokId, LocalDateTime startTime, User whiteStoneUser, User blakcStoneUser) {

        return new GomokHistory(
                gomokId,
                startTime,
                0L,
                whiteStoneUser,
                blakcStoneUser,
                Stone.EMPTY,
                new ArrayList<>()
        );
    }

    public PlaceResult addPlaceResult (int row, int col, Stone stone) {
        Long count = ++placeCount;
        PlaceResult placeResult = new PlaceResult(row, col, stone, count);

        placeResults.add(placeResult);

        return placeResult;
    }
}

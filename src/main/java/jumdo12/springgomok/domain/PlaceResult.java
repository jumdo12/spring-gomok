package jumdo12.springgomok.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class PlaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int placeRow;
    private int placeCol;

    @Enumerated(EnumType.STRING)
    private Stone stone;

    private Long moveOrder;

    public PlaceResult(int placeRow, int placeCol, Stone stone, Long moveOrder) {
        this.placeRow = placeRow;
        this.placeCol = placeCol;
        this.stone = stone;
        this.moveOrder = moveOrder;
    }
}

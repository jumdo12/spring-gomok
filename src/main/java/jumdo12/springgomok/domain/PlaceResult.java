package jumdo12.springgomok.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PlaceResult {

    private int placeRow;
    private int placeCol;

    @Enumerated(EnumType.STRING)
    private Stone stone;

    private Long moveOrder;
}

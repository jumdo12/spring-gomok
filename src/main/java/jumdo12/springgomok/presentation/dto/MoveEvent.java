package jumdo12.springgomok.presentation.dto;

import jumdo12.springgomok.domain.Stone;

public record MoveEvent(
        int row,
        int col,
        String gameStatus,
        Stone winner
) {
}

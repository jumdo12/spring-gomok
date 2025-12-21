package jumdo12.springgomok.presentation.dto;

import java.time.LocalDateTime;

public record GameHistoryResponse(
        String gameId,
        LocalDateTime startTime
) {
}

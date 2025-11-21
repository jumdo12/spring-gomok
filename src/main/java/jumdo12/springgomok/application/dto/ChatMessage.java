package jumdo12.springgomok.application.dto;

import java.time.LocalDateTime;

public record ChatMessage(
        String from,
        LocalDateTime sendAt,
        String content
) {
}

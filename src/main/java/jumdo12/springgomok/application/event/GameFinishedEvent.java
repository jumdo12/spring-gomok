package jumdo12.springgomok.application.event;

import jumdo12.springgomok.domain.Stone;

public record GameFinishedEvent(
        Long roomId,
        String winnerNickname,
        Stone winnerStone
) {

}

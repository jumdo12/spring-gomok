package jumdo12.springgomok.presentation.dto;

public record StoneSwitchedEvent(
        Long blackUserId,
        String blackNickname,
        Long whiteUserId,
        String whiteNickname
) {

}

package jumdo12.springgomok.common.execption;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_MOVE(400, "유효하지 않은 착수입니다"),
    NOT_YOUR_TURN(400, "당신의 차례가 아닙니다"),

    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
    UNAUTHORIZED(401, "인증되지 않은 사용자입니다"),
    FORBIDDEN(403, "권한이 없습니다"),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다"),

    ROOM_NOT_FOUND(404, "방을 찾을 수 없습니다"),
    ROOM_FULL(400, "방이 가득 찼습니다"),
    ALREADY_IN_ROOM(400, "이미 방에 참여 중입니다"),
    NOT_ROOM_PARTICIPANT(403, "방 참여자가 아닙니다"),
    INVALID_ROOM_STATUS(400, "방 상태가 올바르지 않습니다"),

    GAME_RESULT_NOT_FOUND(404, "게임 기록을 찾을 수 없습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

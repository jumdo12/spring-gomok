package jumdo12.springgomok.domain;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Slf4j
public class Gomok {

    private final String id;
    private final Board board;
    private final GomokRule gomokRule;
    private final Turn turn;
    private final List<PlaceRecord> moveHistory = new ArrayList<>();

    private Gomok(String id, Board board, GomokRule gomokRule, Turn turn) {
        this.id = id;
        this.board = board;
        this.turn = turn;
        this.gomokRule = gomokRule;
    }

    public static Gomok create(GomokRule gomokRule, Set<Player> players) {
        Player blackPlayer = players.stream()
                .filter(p -> p.getStone() == Stone.BLACK)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("흑돌 플레이어가 없습니다."));

        Player whitePlayer = players.stream()
                .filter(p -> p.getStone() == Stone.WHITE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("백돌 플레이어가 없습니다."));

        UUID uuid = UUID.randomUUID();
        Board board = new Board(GomokRule.GOMOK_BOARD_SIZE);
        Turn turn = new Turn(blackPlayer, whitePlayer);

        return new Gomok(uuid.toString(), board, gomokRule, turn);
    }

    public MoveResult placeStone(Position position, Player player) {
        validateTurn(player);
        board.place(position, player.getStone());
        moveHistory.add(new PlaceRecord(position, player.getStone()));

        if (gomokRule.isWinningMove(board, position, player.getStone())) {
            return new MoveResult(true);
        }

        turn.next();
        return new MoveResult(false);
    }

    public List<PlaceRecord> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    private void validateTurn(Player player) {
        if (turn.getCurrent() != player) {
            throw new BusinessException(ErrorCode.NOT_YOUR_TURN);
        }
    }
}

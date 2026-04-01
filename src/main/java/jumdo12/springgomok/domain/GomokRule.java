package jumdo12.springgomok.domain;

import java.util.Optional;

public class GomokRule {

    public static final int GOMOK_BOARD_SIZE = 18;
    private static final int[][] DIRECTIONS = {
            {0, 1},
            {1, 0},
            {1, 1},
            {1, -1}
    };

    public boolean isWinningMove(Board board, Position position, Stone stone) {
        for (int[] dir : DIRECTIONS) {
            int count = 1;
            count += countConsecutive(board, position, dir[0], dir[1], stone);
            count += countConsecutive(board, position, -dir[0], -dir[1], stone);

            if (count >= 5) return true;
        }
        return false;
    }

    private int countConsecutive(Board board, Position position, int dr, int dc, Stone stone) {
        int count = 0;
        int r = position.row() + dr;
        int c = position.col() + dc;

        while (r >= 0 && r < GOMOK_BOARD_SIZE && c >= 0 && c < GOMOK_BOARD_SIZE) {
            if (board.get(new Position(r, c)).orElse(null) != stone) break;
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }
}

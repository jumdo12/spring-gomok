package jumdo12.springgomok.domain;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Getter
@Slf4j
public class Gomok {

    private static final int BOARD_SIZE = 18;

    private final Stone[][] grid;
    private final String id;

    private Stone currTurn;
    private Stone winner;

    private Gomok(String id, Stone[][] grid) {
        this.id = id;
        this.grid = grid;
        this.currTurn = Stone.BLACK;
        this.winner = Stone.EMPTY;
    }

    public static Gomok create() {
        Stone[][] grid = new Stone[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                grid[i][j] = Stone.EMPTY;
            }
        }

        return new Gomok(UUID.randomUUID().toString(), grid);
    }

    public static Gomok restore(String id, Stone[][] grid, Stone currTurn, Stone winner) {
        Gomok gomok = new Gomok(id, grid);
        gomok.currTurn = currTurn;
        gomok.winner = winner;

        return gomok;
    }

    public void placeStone(int row, int col, Stone stone) {
        validatePosition(row, col);
        validateTurn(stone);

        grid[row][col] = stone;
        calcWinner(row, col);

        currTurn = currTurn.opposite();
    }

    private void validatePosition(int row, int col) {
        validateBound(row, col);

        if(grid[row][col] != Stone.EMPTY) {
            throw new BusinessException(ErrorCode.INVALID_MOVE);
        }
    }

    private void calcWinner(int row, int col) {
        Stone stone = grid[row][col];

        int[][] directions = {
                {0, 1},
                {1, 0},
                {1, 1},
                {1, -1}
        };

        for (int[] dir : directions) {
            int dr = dir[0];
            int dc = dir[1];

            int count = 1;

            count += countConsecutive(row, col, dr, dc, stone);
            count += countConsecutive(row, col, -dr, -dc, stone);

            if (count == 5) {
                this.winner = stone;
            }
        }
    }

    private void validateTurn(Stone stone) {
        if(stone != currTurn) {
            throw new BusinessException(ErrorCode.NOT_YOUR_TURN);
        }
    }

    private int countConsecutive(int row, int col, int dr, int dc, Stone stone) {
        int count = 0;

        for (int r = row + dr, c = col + dc;
             !isOutBound(r, c) && grid[r][c] == stone;
             r += dr, c += dc) {

            count++;
        }

        return count;
    }

    private void validateBound(int row, int col){
        if(isOutBound(row, col)) {
            throw new BusinessException(ErrorCode.INVALID_MOVE);
        }
    }

    private boolean isOutBound(int row, int col) {
        return row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE;
    }
}

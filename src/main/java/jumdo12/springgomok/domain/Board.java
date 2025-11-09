package jumdo12.springgomok.domain;

import lombok.Getter;

@Getter
public class Board {

    private static final int BOARD_SIZE = 15;

    private final Stone[][] grid;

    private Board(Stone[][] grid) {
        this.grid = grid;
    }

    public static Board create() {
        Stone[][] grid = new Stone[BOARD_SIZE][BOARD_SIZE];

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                grid[i][j] = Stone.EMPTY;
            }
        }

        return new Board(grid);
    }

    public void placeStone(int row, int col, Stone stone) {
        validateBound(row, col);
        validatePosition(row, col);

        grid[row][col] = stone;
    }

    private void validatePosition(int row, int col) {
        if(grid[row][col] != Stone.EMPTY) {
            throw new IllegalArgumentException("착수 위치가 올바르지 않습니다.");
        }
    }

    public Stone calcWinner(int row, int col) {
        Stone stone = grid[row][col];
        if (stone == Stone.EMPTY) {
            return Stone.EMPTY;
        }

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
                return stone;
            }
        }

        return Stone.EMPTY;
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
            throw new IllegalArgumentException("착수 위치가 올바르지 않습니다.");
        }
    }

    private boolean isOutBound(int row, int col) {
        return row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE;
    }
}

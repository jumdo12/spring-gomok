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

    private void validateBound(int row, int col){
        if(row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IllegalArgumentException("착수 위치가 올바르지 않습니다.");
        }
    }
}

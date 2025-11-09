package jumdo12.springgomok.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = Board.create();
    }

    @Test
    void 올바른_위치에_바둑알을_둘_수_있다() {
        int row = 10;
        int col = 10;

        Assertions.assertDoesNotThrow(() -> board.placeStone(row, col, Stone.BLACK));
    }

    @Test
    void 바둑판_밖에_착수할_수_없다() {
        int row = -1;
        int col = -1;

        assertThatThrownBy(
                () -> board.placeStone(row, col, Stone.BLACK)).
                isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 바둑알이_존재하는_위치에_착수할_수_없다() {
        int row = 10;
        int col = 10;

        board.placeStone(row, col, Stone.BLACK);

        assertThatThrownBy(
                () -> board.placeStone(row, col, Stone.BLACK)).
                isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 가로_5목이면_해당_색이_승리한다() {
        int row = 7;

        for (int col = 3; col <= 7; col++) {
            board.placeStone(row, col, Stone.BLACK);
        }

        Stone winner = board.calcWinner(row, 7);

        assertThat(winner).isEqualTo(Stone.BLACK);
    }

    @Test
    void 세로_5목이면_해당_색이_승리한다() {
        int col = 5;

        for (int row = 2; row <= 6; row++) {
            board.placeStone(row, col, Stone.WHITE);
        }

        Stone winner = board.calcWinner(6, col);

        assertThat(winner).isEqualTo(Stone.WHITE);
    }

    @Test
    void 대각선_우하향_5목이면_해당_색이_승리한다() {
        for (int i = 3; i <= 7; i++) {
            board.placeStone(i, i, Stone.BLACK);
        }

        Stone winner = board.calcWinner(7, 7);

        assertThat(winner).isEqualTo(Stone.BLACK);
    }

    @Test
    void 대각선_우상향_5목이면_해당_색이_승리한다() {
        board.placeStone(7, 3, Stone.WHITE);
        board.placeStone(6, 4, Stone.WHITE);
        board.placeStone(5, 5, Stone.WHITE);
        board.placeStone(4, 6, Stone.WHITE);
        board.placeStone(3, 7, Stone.WHITE);

        Stone winner = board.calcWinner(3, 7);

        assertThat(winner).isEqualTo(Stone.WHITE);
    }

    @Test
    void 네개만_연속이면_아직_승리자가_없다() {
        int row = 10;

        for (int col = 1; col <= 4; col++) {
            board.placeStone(row, col, Stone.BLACK);
        }

        Stone winner = board.calcWinner(row, 4);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }

    @Test
    void 다른색_돌이_섞여있으면_승리자가_없다() {
        int row = 5;

        board.placeStone(row, 3, Stone.BLACK);
        board.placeStone(row, 4, Stone.WHITE);
        board.placeStone(row, 5, Stone.BLACK);
        board.placeStone(row, 6, Stone.BLACK);
        board.placeStone(row, 7, Stone.BLACK);

        Stone winner = board.calcWinner(row, 7);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }

    @Test
    void 육목으로_승리할_수_없다() {
        int row = 7;

        for (int col = 3; col <= 8; col++) {
            board.placeStone(row, col, Stone.BLACK);
        }

        Stone winner = board.calcWinner(row, 8);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }
}

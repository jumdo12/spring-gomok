package jumdo12.springgomok.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GomokTest {

    private Gomok gomok;

    @BeforeEach
    void setUp() {
        gomok = Gomok.create();
    }

    @Test
    void 올바른_위치에_바둑알을_둘_수_있다() {
        int row = 10;
        int col = 10;

        Assertions.assertDoesNotThrow(() -> gomok.placeStone(row, col, Stone.BLACK));
    }

    @Test
    void 바둑판_밖에_착수할_수_없다() {
        int row = -1;
        int col = -1;

        assertThatThrownBy(
                () -> gomok.placeStone(row, col, Stone.BLACK)).
                isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 바둑알이_존재하는_위치에_착수할_수_없다() {
        int row = 10;
        int col = 10;

        gomok.placeStone(row, col, Stone.BLACK);

        assertThatThrownBy(
                () -> gomok.placeStone(row, col, Stone.BLACK)).
                isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 가로_5목이면_해당_색이_승리한다() {
        int row = 7;

        for (int col = 3; col <= 7; col++) {
            gomok.placeStone(row, col, Stone.BLACK);
        }

        Stone winner = gomok.calcWinner(row, 7);

        assertThat(winner).isEqualTo(Stone.BLACK);
    }

    @Test
    void 세로_5목이면_해당_색이_승리한다() {
        int col = 5;

        for (int row = 2; row <= 6; row++) {
            gomok.placeStone(row, col, Stone.WHITE);
        }

        Stone winner = gomok.calcWinner(6, col);

        assertThat(winner).isEqualTo(Stone.WHITE);
    }

    @Test
    void 대각선_우하향_5목이면_해당_색이_승리한다() {
        for (int i = 3; i <= 7; i++) {
            gomok.placeStone(i, i, Stone.BLACK);
        }

        Stone winner = gomok.calcWinner(7, 7);

        assertThat(winner).isEqualTo(Stone.BLACK);
    }

    @Test
    void 대각선_우상향_5목이면_해당_색이_승리한다() {
        gomok.placeStone(7, 3, Stone.WHITE);
        gomok.placeStone(6, 4, Stone.WHITE);
        gomok.placeStone(5, 5, Stone.WHITE);
        gomok.placeStone(4, 6, Stone.WHITE);
        gomok.placeStone(3, 7, Stone.WHITE);

        Stone winner = gomok.calcWinner(3, 7);

        assertThat(winner).isEqualTo(Stone.WHITE);
    }

    @Test
    void 네개만_연속이면_아직_승리자가_없다() {
        int row = 10;

        for (int col = 1; col <= 4; col++) {
            gomok.placeStone(row, col, Stone.BLACK);
        }

        Stone winner = gomok.calcWinner(row, 4);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }

    @Test
    void 다른색_돌이_섞여있으면_승리자가_없다() {
        int row = 5;

        gomok.placeStone(row, 3, Stone.BLACK);
        gomok.placeStone(row, 4, Stone.WHITE);
        gomok.placeStone(row, 5, Stone.BLACK);
        gomok.placeStone(row, 6, Stone.BLACK);
        gomok.placeStone(row, 7, Stone.BLACK);

        Stone winner = gomok.calcWinner(row, 7);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }

    @Test
    void 육목으로_승리할_수_없다() {
        int row = 7;

        for (int col = 3; col <= 8; col++) {
            gomok.placeStone(row, col, Stone.BLACK);
        }

        Stone winner = gomok.calcWinner(row, 8);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }
}

package jumdo12.springgomok.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BoardTest {

    private Board board = Board.create();

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
}

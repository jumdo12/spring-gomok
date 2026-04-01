package jumdo12.springgomok.domain;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Board {

    private final Map<Position, Stone> grid = new HashMap<>();
    private final int size;

    public Board(int size) {
        this.size = size;
    }

    public void place(Position position, Stone stone) {
        validatePosition(position);

        grid.put(position, stone);
    }

    private void validatePosition(Position position) {
        if (position.row() < 0 || position.row() >= size ||
                position.col() < 0 || position.col() >= size) {
            throw new BusinessException(ErrorCode.INVALID_MOVE);
        }

        if (grid.containsKey(position)) {
            throw new BusinessException(ErrorCode.INVALID_MOVE);
        }
    }

    public Optional<Stone> get(Position position) {
        return Optional.ofNullable(grid.get(position));
    }
}

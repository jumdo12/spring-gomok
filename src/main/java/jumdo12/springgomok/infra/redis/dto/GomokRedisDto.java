package jumdo12.springgomok.infra.redis.dto;

import jumdo12.springgomok.domain.Gomok;
import jumdo12.springgomok.domain.Stone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GomokRedisDto {

    private String id;
    private String[][] grid;
    private Stone currTurn;
    private Stone winner;

    public static GomokRedisDto from(Gomok gomok) {
        Stone[][] stoneGrid = gomok.getGrid();
        String[][] stringGrid = Arrays.stream(stoneGrid)
                .map(row -> Arrays.stream(row)
                        .map(Stone::name)
                        .toArray(String[]::new))
                .toArray(String[][]::new);

        return new GomokRedisDto(gomok.getId(), stringGrid, gomok.getCurrTurn(), gomok.getWinner());
    }
}

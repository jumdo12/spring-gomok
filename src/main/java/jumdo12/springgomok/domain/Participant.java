package jumdo12.springgomok.domain;

import lombok.Getter;

@Getter
public class Participant {

    private final User user;
    private Stone stone;

    public Participant(User user, Stone stone) {
        this.user = user;
        this.stone = stone;
    }

    public void switchStone() {
        if (stone == Stone.EMPTY) {
            return;
        }
        stone = (stone == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    }
}

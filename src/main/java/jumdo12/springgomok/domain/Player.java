package jumdo12.springgomok.domain;

import lombok.Getter;

@Getter
public class Player {

    private final User user;
    private Stone stone;

    public Player(User user, Stone stone) {
        this.user = user;
        this.stone = stone;
    }

    public void switchStone() {
        this.stone = this.stone.opposite();
    }
}

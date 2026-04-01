package jumdo12.springgomok.domain;

import lombok.Getter;

public class Turn {

    private final Player black;
    private final Player white;

    @Getter
    private Player current;

    public Turn(Player black, Player white) {
        this.black = black;
        this.white = white;

        this.current = black;
    }

    public void next() {
        current = current == black ? white : black;
    }
}

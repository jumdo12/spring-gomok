package jumdo12.springgomok.domain;

import lombok.Getter;

@Getter
public class Participant {

    private final User user;
    private final Stone stone;

    public Participant(User user, Stone stone) {
        this.user = user;
        this.stone = stone;
    }
}

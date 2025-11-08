package jumdo12.springgomok.domain;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class Room {

    private Long id;
    private String roomName;
    private User host;
    private Set<Participant> participants;
    private Board board;

    private Room(Long id, String roomName, User host) {
        this.id = id;
        this.roomName = roomName;
        this.host = host;
        this.board = Board.create();

        this.participants = new HashSet<>();
        participants.add(new Participant(host, Stone.BLACK));
    }

    public static Room create(Long id, String roomName, User host) {
        return new Room(id, roomName, host);
    }
}

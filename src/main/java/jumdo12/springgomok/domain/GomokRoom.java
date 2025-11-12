package jumdo12.springgomok.domain;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GomokRoom {

    private final static int PARTICIPANT_COUNT = 2;

    private Long id;
    private String roomName;
    private User host;
    private Set<Participant> participants;
    private Gomok gomok;

    private GomokRoom(Long id, String roomName, User host) {
        this.id = id;
        this.roomName = roomName;
        this.host = host;
        this.gomok = Gomok.create();

        this.participants = new HashSet<>();
        participants.add(new Participant(host, Stone.BLACK));
    }

    public static GomokRoom create(Long id, String roomName, User host) {
        return new GomokRoom(id, roomName, host);
    }

    public void join(User user) {
        if (participants.size() >= PARTICIPANT_COUNT) {
            throw new IllegalArgumentException("방이 이미 가득 찼습니다");
        }

        boolean alreadyJoined = participants.stream()
                .anyMatch(p -> p.getUser().equals(user));
        if (alreadyJoined) {
            throw new IllegalArgumentException("이미 참가한 유저입니다");
        }

        Stone stone = getAvailableStone();
        participants.add(new Participant(user, stone));
    }

    public void switchParticipantsStone(User user) {
        for (Participant participant : participants) {
            participant.switchStone();
        }
    }

    public boolean isHost(User user) {
        return host.equals(user);
    }

    public void leave(User user) {
        boolean removeIf = participants.removeIf(p -> p.getUser().equals(user));

        if(!removeIf) {
            throw new IllegalArgumentException("참가 유저가 아닙니다.");
        }
    }

    private Stone getAvailableStone() {
        boolean blackUsed = participants.stream()
                .anyMatch(p -> p.getStone() == Stone.BLACK);
        boolean whiteUsed = participants.stream()
                .anyMatch(p -> p.getStone() == Stone.WHITE);

        if (!blackUsed) {
            return Stone.BLACK;
        }
        if (!whiteUsed) {
            return Stone.WHITE;
        }

        throw new IllegalStateException("배정할 돌이 없습니다.");
    }
}

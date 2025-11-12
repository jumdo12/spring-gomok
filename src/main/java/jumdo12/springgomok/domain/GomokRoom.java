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
    private GomokRoomStatus gomokRoomStatus;
    private Stone winner;

    private GomokRoom(Long id, String roomName, User host) {
        this.id = id;
        this.roomName = roomName;
        this.host = host;
        this.gomok = Gomok.create();
        this.gomokRoomStatus = GomokRoomStatus.WAITING;
        this.winner = Stone.EMPTY;

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
        gomokRoomStatus = GomokRoomStatus.READY;

        participants.add(new Participant(user, stone));
    }

    public void startGomok() {
        if(gomokRoomStatus != GomokRoomStatus.READY) {
            throw new IllegalArgumentException("준비가 완료되지 않았습니다");
        }

        gomokRoomStatus = GomokRoomStatus.PLAYING;
    }

    public void switchParticipantsStone() {
        for (Participant participant : participants) {
            participant.switchStone();
        }
    }

    public boolean isHost(User user) {
        return host.equals(user);
    }

    public void leave(User user) {
        Participant participant = getParticipant(user);

        if(gomokRoomStatus == GomokRoomStatus.PLAYING) {
            throw new IllegalArgumentException("게임이 진행 중 입니다");
        }

        gomokRoomStatus = GomokRoomStatus.WAITING;
        participants.remove(participant);
    }

    public void placeGomokStone(int row, int col, User user) {
        if(gomokRoomStatus != GomokRoomStatus.PLAYING) {
            throw new IllegalArgumentException("게임 진행 중에만 착수할 수 있습니다");
        }

        Participant participant = getParticipant(user);

        if(participant.getStone() != gomok.getCurrTurn()) {
            throw new IllegalArgumentException("상대방의 차례입니다.");
        }

        gomok.placeStone(row, col, participant.getStone());

        Stone winner = gomok.calcWinner(row, col);

        if(winner != Stone.EMPTY) {
            gomokRoomStatus = GomokRoomStatus.FINISHED;
            this.winner = winner;
        }
    }

    public Stone getWinner(User user) {
        Participant participant = getParticipant(user);

        return winner;
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

    private Participant getParticipant(User user) {
        return participants.stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("참가자를 찾을 수 없습니다."));
    }
}

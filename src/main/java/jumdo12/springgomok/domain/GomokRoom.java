package jumdo12.springgomok.domain;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
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

    private GomokRoom(Long id, String roomName, User host) {
        this.id = id;
        this.roomName = roomName;
        this.host = host;
        this.gomok = Gomok.create();
        this.gomokRoomStatus = GomokRoomStatus.WAITING;

        this.participants = new HashSet<>();
        participants.add(new Participant(host, Stone.BLACK));
    }

    public static GomokRoom create(String roomName, User host) {
        return new GomokRoom(null, roomName, host);
    }

    public static GomokRoom restore(
            Long id,
            String roomName,
            GomokRoomStatus status,
            User host, Set<Participant> participants,
            Gomok gomok
    ) {
        GomokRoom room = new GomokRoom(id, roomName, host);
        room.gomokRoomStatus = status;
        room.participants = participants;
        room.gomok = gomok;
        return room;
    }

    public void join(User user) {
        if (participants.size() >= PARTICIPANT_COUNT) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }

        boolean alreadyJoined = participants.stream()
                .anyMatch(p -> p.getUser().equals(user));
        if (alreadyJoined) {
            throw new BusinessException(ErrorCode.ALREADY_IN_ROOM);
        }

        Stone stone = getAvailableStone();
        gomokRoomStatus = GomokRoomStatus.READY;

        participants.add(new Participant(user, stone));
    }

    public void startGomok(User user) {
        if(!isHost(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if(gomokRoomStatus != GomokRoomStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
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
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        gomokRoomStatus = GomokRoomStatus.WAITING;
        participants.remove(participant);
    }

    public Stone placeGomokStone(int row, int col, User user) {
        if(gomokRoomStatus != GomokRoomStatus.PLAYING) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        Participant participant = getParticipant(user);

        gomok.placeStone(row, col, participant.getStone());

        Stone winner = gomok.getWinner();

        if(winner != Stone.EMPTY) {
            gomokRoomStatus = GomokRoomStatus.FINISHED;
        }

        return participant.getStone();
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ROOM_PARTICIPANT));
    }

    public Stone getWinner() {
        return gomok.getWinner();
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public String getGomokGameId() {
        return gomok.getId();
    }
}

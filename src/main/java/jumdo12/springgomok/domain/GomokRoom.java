package jumdo12.springgomok.domain;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class GomokRoom {

    private final static int PARTICIPANT_COUNT = 2;

    private Long id;
    private String roomName;
    private User host;
    private Set<Player> players = new HashSet<>();
    private Gomok gomok;
    private GomokRoomStatus gomokRoomStatus;
    private Player winner;

    private GomokRoom(
            Long id,
            String roomName,
            User host,
            Gomok gomok,
            GomokRoomStatus gomokRoomStatus
    ) {
        this.id = id;
        this.roomName = roomName;
        this.host = host;
        this.gomok = gomok;
        this.gomokRoomStatus = gomokRoomStatus;
    }

    public static GomokRoom create(String roomName, User host) {
        GomokRoom room = new GomokRoom(null, roomName, host, null, GomokRoomStatus.WAITING);
        room.players.add(new Player(host, Stone.BLACK));
        return room;
    }

    public void join(User user) {
        if (players.size() >= PARTICIPANT_COUNT) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }

        boolean alreadyJoined = players.stream()
                .anyMatch(p -> p.getUser().equals(user));
        if (alreadyJoined) {
            throw new BusinessException(ErrorCode.ALREADY_IN_ROOM);
        }

        Stone stone = getAvailableStone();
        gomokRoomStatus = GomokRoomStatus.READY;
        players.add(new Player(user, stone));
    }

    public void startGomok(Player player) {
        if (!isHost(player)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (gomokRoomStatus != GomokRoomStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        this.gomok = Gomok.create(new GomokRule(), players);
        gomokRoomStatus = GomokRoomStatus.PLAYING;
    }

    public void switchParticipantsStone() {
        if (gomokRoomStatus != GomokRoomStatus.WAITING && gomokRoomStatus != GomokRoomStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }
        for (Player player : players) {
            player.switchStone();
        }
    }

    public boolean isHost(Player player) {
        return host.equals(player.getUser());
    }

    public void leave(Player player) {
        if (gomokRoomStatus == GomokRoomStatus.PLAYING) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        players.remove(player);

        if (players.isEmpty()) {
            gomokRoomStatus = GomokRoomStatus.CLOSED;
            return;
        }

        if (isHost(player)) {
            host = players.iterator().next().getUser();
        }

        gomokRoomStatus = GomokRoomStatus.WAITING;
    }

    public MoveResult placeGomokStone(Position position, Player player) {
        if (gomokRoomStatus != GomokRoomStatus.PLAYING) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        MoveResult moveResult = gomok.placeStone(position, player);

        if (moveResult.isWinningMove()) {
            this.winner = player;
            gomokRoomStatus = GomokRoomStatus.FINISHED;
        }

        return moveResult;
    }

    public Player findPlayer(User user) {
        return players.stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ROOM_PARTICIPANT));
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID가 이미 존재합니다.");
        }
        this.id = id;
    }

    public int getParticipantCount() {
        return players.size();
    }

    public Map<Position, Stone> getBoardGrid() {
        return gomok.getBoard().getGrid();
    }

    public String getGomokGameId() {
        return gomok.getId();
    }

    private Stone getAvailableStone() {
        boolean blackUsed = players.stream().anyMatch(p -> p.getStone() == Stone.BLACK);
        boolean whiteUsed = players.stream().anyMatch(p -> p.getStone() == Stone.WHITE);

        if (!blackUsed) return Stone.BLACK;
        if (!whiteUsed) return Stone.WHITE;

        throw new IllegalStateException("배정할 돌이 없습니다.");
    }
}

package jumdo12.springgomok.application;

import jumdo12.springgomok.application.dto.ChatMessage;
import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.application.dto.GameRoomInfo;
import jumdo12.springgomok.application.event.RoomUpdatedEvent;
import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.dto.ChatRequest;
import jumdo12.springgomok.presentation.dto.PlacedStone;
import jumdo12.springgomok.presentation.dto.StoneSwitchedEvent;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GomokRoomService {

    private final GomokRoomRepository gomokRoomRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GomokRoom createRoom(String roomName, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = GomokRoom.create(roomName, user);
        return gomokRoomRepository.save(room);
    }

    public void joinRoom(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);

        room.join(user);
        gomokRoomRepository.update(room);
        eventPublisher.publishEvent(new RoomUpdatedEvent(roomId, room));
    }

    public void leaveRoom(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);
        Player player = room.findPlayer(user);

        room.leave(player);

        if (room.getGomokRoomStatus() == GomokRoomStatus.CLOSED) {
            gomokRoomRepository.deleteById(roomId);
            return;
        }

        gomokRoomRepository.update(room);
        eventPublisher.publishEvent(new RoomUpdatedEvent(roomId, room));
    }

    public void startGame(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);
        Player player = room.findPlayer(user);

        room.startGomok(player);
        gomokRoomRepository.update(room);
    }

    public GameRoomDetailInfo getGameDetailInfo(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);
        return GameRoomDetailInfo.from(room, user.getId());
    }

    public List<GameRoomInfo> getWaitingRooms() {
        return gomokRoomRepository.findAll().stream()
                .filter(r -> r.getGomokRoomStatus() == GomokRoomStatus.WAITING)
                .map(GameRoomInfo::from)
                .toList();
    }

    public ChatMessage sendChatMessage(Long roomId, LoginUser loginUser, ChatRequest chatMessage) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);
        room.findPlayer(user);

        return new ChatMessage(user.getNickname(), LocalDateTime.now(), chatMessage.content());
    }

    public List<PlacedStone> getBoardState(Long roomId) {
        GomokRoom room = findRoom(roomId);

        if (room.getGomokRoomStatus() != GomokRoomStatus.PLAYING &&
                room.getGomokRoomStatus() != GomokRoomStatus.FINISHED) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_STATUS);
        }

        return room.getBoardGrid().entrySet().stream()
                .map(e -> new PlacedStone(e.getKey().row(), e.getKey().col(), e.getValue()))
                .toList();
    }

    public StoneSwitchedEvent switchStone(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);
        Player player = room.findPlayer(user);

        if (!room.isHost(player)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        room.switchParticipantsStone();
        gomokRoomRepository.update(room);

        return buildStoneSwitchedEvent(room.getPlayers());
    }

    public GomokRoom findRoom(Long roomId) {
        return gomokRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    private StoneSwitchedEvent buildStoneSwitchedEvent(Set<Player> players) {
        Player black = findPlayerByStone(players, Stone.BLACK);
        Player white = findPlayerByStone(players, Stone.WHITE);

        return new StoneSwitchedEvent(
                black != null ? black.getUser().getId() : null,
                black != null ? black.getUser().getNickname() : null,
                white != null ? white.getUser().getId() : null,
                white != null ? white.getUser().getNickname() : null
        );
    }

    private Player findPlayerByStone(Set<Player> players, Stone stone) {
        return players.stream()
                .filter(p -> p.getStone() == stone)
                .findFirst()
                .orElse(null);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}

package jumdo12.springgomok.application;

import jumdo12.springgomok.application.dto.ChatMessage;
import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.application.dto.GameRoomInfo;
import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GomokRoomService {

    private final GomokRoomRepository gomokRoomRepository;
    private final UserRepository userRepository;

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
    }

    public void leaveRoom(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);

        if (room.getParticipantCount() == 1) {
            gomokRoomRepository.deleteById(roomId);
            return;
        }

        room.leave(user);

        if (room.getGomokRoomStatus() == GomokRoomStatus.CLOSED) {
            gomokRoomRepository.deleteById(roomId);
            return;
        }

        gomokRoomRepository.update(room);
    }

    public void startGame(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());
        GomokRoom room = findRoom(roomId);

        room.startGomok(user);
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

    public ChatMessage sendChatMessage(LoginUser loginUser, String content) {
        User user = findUser(loginUser.id());
        return new ChatMessage(user.getNickname(), LocalDateTime.now(), content);
    }

    public GomokRoom findRoom(Long roomId) {
        return gomokRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}

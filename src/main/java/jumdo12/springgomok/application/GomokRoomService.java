package jumdo12.springgomok.application;

import jumdo12.springgomok.application.dto.GameRoomDetailInfo;
import jumdo12.springgomok.application.dto.GameRoomInfo;
import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GomokRoomService {

    private final GomokRooms gomokRooms;
    private final UserRepository userRepository;

    public void joinRoom(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());

        gomokRooms.joinRoom(roomId, user);
    }

    public void leaveRoom(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());

        gomokRooms.leaveRoom(roomId, user);
    }

    public void startGame(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());

        gomokRooms.startGame(roomId, user);
    }

    public GameRoomDetailInfo getGameDetailInfo(Long roomId, LoginUser loginUser) {
        User user = findUser(loginUser.id());

        GomokRoom room = findRoom(roomId);

        return GameRoomDetailInfo.from(room, user.getId());
    }

    public List<GameRoomInfo> getWaitingRooms() {
        List<GomokRoom> waitingRooms = gomokRooms.getWaitingRooms();

        return waitingRooms.stream()
                .map(GameRoomInfo::from)
                .toList();
    }

    public Stone getGomokWinner(Long roomId, LoginUser loginUser){
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());

        return gomokRoom.getWinner(user);
    }

    private GomokRoom findRoom(Long roomId) {
        return gomokRooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방 입니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

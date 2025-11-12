package jumdo12.springgomok.application;

import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.domain.GomokRooms;
import jumdo12.springgomok.domain.User;
import jumdo12.springgomok.domain.UserRepository;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    private GomokRoom findRoom(Long roomId) {
        return gomokRooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방 입니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

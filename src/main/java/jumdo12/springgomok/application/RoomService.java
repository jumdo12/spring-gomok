package jumdo12.springgomok.application;

import jumdo12.springgomok.domain.Room;
import jumdo12.springgomok.domain.Rooms;
import jumdo12.springgomok.domain.User;
import jumdo12.springgomok.domain.UserRepository;
import jumdo12.springgomok.presentation.resolver.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final Rooms rooms;
    private final UserRepository userRepository;

    public void joinRoom(Long roomId, LoginMember loginMember) {
        User user = findUser(loginMember.id());

        rooms.joinRoom(roomId, user);
    }

    public void leaveRoom(Long roomId, LoginMember loginMember) {
        User user = findUser(loginMember.id());

        rooms.leaveRoom(roomId, user);
    }

    private Room findRoom(Long roomId) {
        return rooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방 입니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

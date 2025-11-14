package jumdo12.springgomok.application;

import jumdo12.springgomok.domain.*;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GomokService {

    private final GomokRooms gomokRooms;
    private final UserRepository userRepository;

    public void placeGomok(Long roomId, LoginUser loginUser, int row, int col){
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());

        gomokRoom.placeGomokStone(row, col, user);
    }

    public void leaveGomok(Long roomId, LoginUser loginUser){
        GomokRoom gomokRoom = findRoom(roomId);

        User user = findUser(loginUser.id());

        gomokRoom.leave(user);
    }

    public void switchStone(Long roomId, LoginUser loginUser) {
        GomokRoom gomokRoom = findRoom(roomId);
        User user = findUser(loginUser.id());

        if (!gomokRoom.isHost(user)) {
            throw new IllegalArgumentException("방장만 돌을 바꿀 수 있습니다.");
        }

        if (gomokRoom.getGomokRoomStatus() != GomokRoomStatus.READY) {
            throw new IllegalArgumentException("준비 상태에서만 돌을 바꿀 수 있습니다.");
        }

        gomokRoom.switchParticipantsStone();
    }

    private GomokRoom findRoom(Long roomId){
        return gomokRooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다"));
    }

    private User findUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

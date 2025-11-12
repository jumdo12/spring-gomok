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
        GomokRoom gomokRoom = getGomokRoom(roomId);
        User user = getUser(loginUser.id());

        gomokRoom.placeGomokStone(row, col, user);
    }

    public Stone getGomokWinner(Long roomId, LoginUser loginUser){
        GomokRoom gomokRoom = getGomokRoom(roomId);
        User user = getUser(loginUser.id());

        return gomokRoom.getWinner(user);
    }

    private GomokRoom getGomokRoom(Long roomId){
        return gomokRooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다"));
    }

    private User getUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

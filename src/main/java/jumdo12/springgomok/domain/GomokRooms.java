package jumdo12.springgomok.domain;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GomokRooms {

    private final Map<Long, GomokRoom> gomokRooms;
    private final AtomicLong counter;

    public GomokRooms() {
        this.gomokRooms = new ConcurrentHashMap<>();
        this.counter = new AtomicLong(0);
    }

    public GomokRoom createRoom(String roomName, User user) {
        Long roomId = counter.incrementAndGet();
        GomokRoom room = GomokRoom.create(roomId, roomName, user);

        gomokRooms.put(roomId, room);
        return room;
    }

    public void joinRoom(Long roomId, User user) {
        GomokRoom room = getRoom(roomId);

        room.join(user);
    }

    public void leaveRoom(Long roomId, User user) {
        GomokRoom room = getRoom(roomId);

        if(room.isHost(user)) {
            gomokRooms.remove(roomId);

            return;
        }

        room.leave(user);
    }

    public Optional<GomokRoom> findById(Long roomId) {
        return Optional.ofNullable(gomokRooms.get(roomId));
    }

    public void remove(Long id) {
        gomokRooms.remove(id);
    }

    public void startGame(Long roomId, User user) {
        GomokRoom room = getRoom(roomId);

        room.startGomok(user);
    }

    public List<GomokRoom> findAll() {
        return gomokRooms.values().stream().toList();
    }

    private GomokRoom getRoom(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
    }
}

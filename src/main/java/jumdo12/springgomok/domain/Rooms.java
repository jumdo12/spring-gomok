package jumdo12.springgomok.domain;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class Rooms {

    private final Map<Long, Room> rooms;
    private final AtomicLong counter;

    public Rooms() {
        this.rooms = new ConcurrentHashMap<>();
        this.counter = new AtomicLong(0);
    }

    public Room createRoom(String roomName, User user) {
        Long roomId = counter.incrementAndGet();
        Room room = Room.create(roomId, roomName, user);

        rooms.put(roomId, room);
        return room;
    }

    public void joinRoom(Long roomId, User user) {
        Room room = getRoom(roomId);

        room.join(user);
    }

    public void leaveRoom(Long roomId, User user) {
        Room room = getRoom(roomId);

        if(room.isHost(user)) {
            rooms.remove(roomId);

            return;
        }

        room.leave(user);
    }

    public Optional<Room> findById(Long roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public void remove(Long id) {
        rooms.remove(id);
    }

    public List<Room> findAll() {
        return rooms.values().stream().toList();
    }

    private Room getRoom(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
    }
}

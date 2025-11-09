package jumdo12.springgomok.domain;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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

        return rooms.put(roomId, room);
    }

    public Room findById(Long roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 방 입니다.");
        }

        return room;
    }

    public void remove(Long id) {
        rooms.remove(id);
    }

    public List<Room> findAll() {
        return rooms.values().stream().toList();
    }
}

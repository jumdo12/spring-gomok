package jumdo12.springgomok.infra.memory;

import jumdo12.springgomok.domain.GomokRoom;
import jumdo12.springgomok.domain.GomokRoomRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryGomokRoomRepository implements GomokRoomRepository {

    private final Map<Long, GomokRoom> store = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public GomokRoom save(GomokRoom room) {
        room.assignId(counter.incrementAndGet());
        store.put(room.getId(), room);
        return room;
    }

    @Override
    public void update(GomokRoom room) {
        store.put(room.getId(), room);
    }

    @Override
    public Optional<GomokRoom> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<GomokRoom> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}

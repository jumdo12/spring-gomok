package jumdo12.springgomok.domain;

import java.util.List;
import java.util.Optional;

public interface GomokRoomRepository {

    GomokRoom save(GomokRoom room);

    void update(GomokRoom room);

    Optional<GomokRoom> findById(Long id);

    List<GomokRoom> findAll();

    void deleteById(Long id);
}

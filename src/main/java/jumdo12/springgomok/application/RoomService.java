package jumdo12.springgomok.application;

import jumdo12.springgomok.domain.Rooms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final Rooms rooms;
}

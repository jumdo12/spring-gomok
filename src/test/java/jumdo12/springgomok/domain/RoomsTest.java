package jumdo12.springgomok.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomsTest {

    private Rooms rooms;
    private User host1;
    private User host2;

    @BeforeEach
    void setUp() {
        rooms = new Rooms();
        host1 = User.create("host1","userId","password");
        host2 = User.create("host2","userId","password");
    }

    @Test
    void 방을_생성하면_저장소에_추가된다() {
        Room room = rooms.createRoom("오목1번방", host1);

        List<Room> allRooms = rooms.findAll();
        assertThat(allRooms).hasSize(1);

        Room saved = allRooms.getFirst();
        assertThat(saved.getRoomName()).isEqualTo("오목1번방");
        assertThat(saved.getHost()).isEqualTo(host1);
    }

    @Test
    void 여러개의_방을_생성하면_ID가_자동으로_증가한다() {
        rooms.createRoom("1번방", host1);
        rooms.createRoom("2번방", host2);

        List<Room> all = rooms.findAll();
        assertThat(all).hasSize(2);

        assertThat(all.get(0).getId()).isLessThan(all.get(1).getId());
    }

    @Test
    void 존재하는_방을_ID로_조회할_수_있다() {
        rooms.createRoom("테스트방", host1);

        Room found = rooms.findById(1L).get();

        assertThat(found.getRoomName()).isEqualTo("테스트방");
        assertThat(found.getHost()).isEqualTo(host1);
    }

    @Test
    void 방을_삭제하면_저장소에서_사라진다() {
        rooms.createRoom("삭제테스트방", host1);

        assertThat(rooms.findAll()).hasSize(1);

        rooms.remove(1L);

        assertThat(rooms.findAll()).isEmpty();
    }

    @Test
    void 방목록을_조회하면_모든_방이_반환된다() {
        rooms.createRoom("1번", host1);
        rooms.createRoom("2번", host2);

        List<Room> all = rooms.findAll();

        assertThat(all).extracting(Room::getRoomName)
                .containsExactlyInAnyOrder("1번", "2번");
    }

    @Test
    void 방장이_방에서_나가면_방을_삭제한다() {
        Room room = rooms.createRoom("1번", host1);

        rooms.leaveRoom(room.getId(), host1);

        assertThat(rooms.findAll()).hasSize(0);
    }

    @Test
    void 방장이_아니라면_방을_나가도_방이_삭제되지_않는다() {
        Room room = rooms.createRoom("1번", host1);
        rooms.joinRoom(room.getId(), host2);

        rooms.leaveRoom(room.getId(), host2);

        assertThat(rooms.findAll()).hasSize(1);
    }
}

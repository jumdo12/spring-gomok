package jumdo12.springgomok.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GomokRoomsTest {

    private GomokRooms gomokRooms;
    private User host1;
    private User host2;

    @BeforeEach
    void setUp() {
        gomokRooms = new GomokRooms();
        host1 = User.create("host1","userId","password");
        host2 = User.create("host2","userId","password");
    }

    @Test
    void 방을_생성하면_저장소에_추가된다() {
        GomokRoom room = gomokRooms.createRoom("오목1번방", host1);

        List<GomokRoom> allRooms = gomokRooms.findAll();
        assertThat(allRooms).hasSize(1);

        GomokRoom saved = allRooms.getFirst();
        assertThat(saved.getRoomName()).isEqualTo("오목1번방");
        assertThat(saved.getHost()).isEqualTo(host1);
    }

    @Test
    void 여러개의_방을_생성하면_ID가_자동으로_증가한다() {
        gomokRooms.createRoom("1번방", host1);
        gomokRooms.createRoom("2번방", host2);

        List<GomokRoom> all = gomokRooms.findAll();
        assertThat(all).hasSize(2);

        assertThat(all.get(0).getId()).isLessThan(all.get(1).getId());
    }

    @Test
    void 존재하는_방을_ID로_조회할_수_있다() {
        gomokRooms.createRoom("테스트방", host1);

        GomokRoom found = gomokRooms.findById(1L).get();

        assertThat(found.getRoomName()).isEqualTo("테스트방");
        assertThat(found.getHost()).isEqualTo(host1);
    }

    @Test
    void 방을_삭제하면_저장소에서_사라진다() {
        gomokRooms.createRoom("삭제테스트방", host1);

        assertThat(gomokRooms.findAll()).hasSize(1);

        gomokRooms.remove(1L);

        assertThat(gomokRooms.findAll()).isEmpty();
    }

    @Test
    void 방목록을_조회하면_모든_방이_반환된다() {
        gomokRooms.createRoom("1번", host1);
        gomokRooms.createRoom("2번", host2);

        List<GomokRoom> all = gomokRooms.findAll();

        assertThat(all).extracting(GomokRoom::getRoomName)
                .containsExactlyInAnyOrder("1번", "2번");
    }

    @Test
    void 방장이_방에서_나가면_방을_삭제한다() {
        GomokRoom room = gomokRooms.createRoom("1번", host1);

        gomokRooms.leaveRoom(room.getId(), host1);

        assertThat(gomokRooms.findAll()).hasSize(0);
    }

    @Test
    void 방장이_아니라면_방을_나가도_방이_삭제되지_않는다() {
        GomokRoom room = gomokRooms.createRoom("1번", host1);
        gomokRooms.joinRoom(room.getId(), host2);

        gomokRooms.leaveRoom(room.getId(), host2);

        assertThat(gomokRooms.findAll()).hasSize(1);
    }
}

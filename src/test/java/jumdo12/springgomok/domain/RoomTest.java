package jumdo12.springgomok.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class RoomTest {

    private User host;
    private User guest;
    private Room room;

    @BeforeEach
    void setUp() {
        host = User.create("host","userId","password");
        guest = User.create("guest","userId","password");

        room = Room.create(1L, "1번방", host);
    }

    @Test
    void 방_생성시_보드가_생성되고_호스트가_흑돌로_참가자에_등록된다() {
        assertThat(room.getBoard()).isNotNull();
        assertThat(room.getRoomName()).isEqualTo("1번방");
        assertThat(room.getHost()).isEqualTo(host);

        assertThat(room.getParticipants()).hasSize(1);

        Participant hostParticipant = room.getParticipants()
                .iterator()
                .next();

        assertThat(hostParticipant.getUser()).isEqualTo(host);
        assertThat(hostParticipant.getStone()).isEqualTo(Stone.BLACK);
    }

    @Test
    void 방에_참가하면_남은_색인_백돌을_배정받는다() {
        room.join(guest);

        assertThat(room.getParticipants()).hasSize(2);

        Optional<Participant> guestParticipant = room.getParticipants().stream()
                .filter(p -> p.getUser().equals(guest))
                .findFirst();

        assertThat(guestParticipant).isPresent();
        assertThat(guestParticipant.get().getStone()).isEqualTo(Stone.WHITE);
    }

    @Test
    void 가득_찬_방에는_더_이상_참가할_수_없다() {
        room.join(guest); // 2명 채움

        User another = User.create("another","userId","password");

        assertThatThrownBy(() -> room.join(another))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방이 이미 가득 찼습니다");
    }

    @Test
    void 같은_유저는_두번_참가할_수_없다() {
        room.join(guest);

        assertThatThrownBy(() -> room.join(guest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 방장이_색을_바꾸면_둘의_색이_서로_바뀐다() {
        Room room = Room.create(1L, "테스트방", host);
        room.join(guest);

        room.switchParticipantsStone(host);

        Participant hostP = room.getParticipants().stream()
                .filter(p -> p.getUser().equals(host))
                .findFirst().get();
        Participant guestP = room.getParticipants().stream()
                .filter(p -> p.getUser().equals(guest))
                .findFirst().get();

        assertThat(hostP.getStone()).isEqualTo(Stone.WHITE);
        assertThat(guestP.getStone()).isEqualTo(Stone.BLACK);
    }

    @Test
    void 호스트_판별이_정상적으로_동작한다() {
        assertThat(room.isHost(host)).isTrue();

        assertThat(room.isHost(guest)).isFalse();
    }

    @Test
    void 참가자가_퇴장하면_목록에서_제거된다() {
        // given
        room.join(guest);
        assertThat(room.getParticipants()).hasSize(2);

        // when
        room.leave(guest);

        // then
        assertThat(room.getParticipants())
                .hasSize(1)
                .allMatch(p -> p.getUser().equals(host));
    }

    @Test
    void 참가자가_아닌_유저가_퇴장하려_하면_예외가_발생한다() {
        User stranger = User.create("stranger", "userId", "password");

        assertThatThrownBy(() -> room.leave(stranger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참가 유저가 아닙니다");
    }
}

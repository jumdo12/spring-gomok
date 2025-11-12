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
    private GomokRoom room;

    @BeforeEach
    void setUp() {
        host = User.create("host","userId","password");
        guest = User.create("guest","userId","password");

        room = GomokRoom.create(1L, "1번방", host);
    }

    @Test
    void 방_생성시_보드가_생성되고_호스트가_흑돌로_참가자에_등록된다() {
        assertThat(room.getGomok()).isNotNull();
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
    void 색을_바꾸면_둘의_색이_서로_바뀐다() {
        GomokRoom room = GomokRoom.create(1L, "테스트방", host);
        room.join(guest);

        room.switchParticipantsStone();

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
        room.join(guest);
        assertThat(room.getParticipants()).hasSize(2);

        room.leave(guest);

        assertThat(room.getParticipants())
                .hasSize(1)
                .allMatch(p -> p.getUser().equals(host));
    }

    @Test
    void 참가자가_아닌_유저가_퇴장하려_하면_예외가_발생한다() {
        User stranger = User.create("stranger", "userId", "password");

        assertThatThrownBy(() -> room.leave(stranger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참가자를 찾을 수 없습니다.");
    }

    @Test
    void 게임_진행_중이_아닐_때_착수하면_예외가_발생한다() {
        room.join(guest);

        assertThatThrownBy(() -> room.placeGomokStone(7, 7, host))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게임 진행 중에만 착수할 수 있습니다");
    }

    @Test
    void 참가자가_아닌_유저가_착수하면_예외가_발생한다() {
        User stranger = User.create("stranger", "userId", "password");
        User participant = User.create("participant", "userId", "password");

        room.join(participant);
        room.startGomok(host);

        assertThatThrownBy(() -> room.placeGomokStone(7, 7, stranger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참가자를 찾을 수 없습니다");
    }

    @Test
    void 게임_진행_중_참가자가_착수하면_정상적으로_돌이_놓인다() {
        room = GomokRoom.create(1L, "테스트방", host);
        room.join(guest);
        room.startGomok(host);

        room.placeGomokStone(7, 7, host);
        Stone result = room.getWinner(host);

        assertThat(result).isEqualTo(Stone.EMPTY); // 아직 승자 없음
        assertThat(room.getGomok().getGrid()[7][7]).isEqualTo(Stone.BLACK);
    }

    @Test
    void 착수_후_5목을_완성하면_승자를_반환한다() {
        room.join(guest);
        room.startGomok(host);

        // host(BLACK)가 가로로 5목을 만들기 위해 (7,3)~(7,7)에 착수
        // guest(WHITE)는 그 사이에 다른 위치에 착수
        room.placeGomokStone(7, 3, host);
        room.placeGomokStone(8, 3, guest);

        room.placeGomokStone(7, 4, host);
        room.placeGomokStone(8, 4, guest);

        room.placeGomokStone(7, 5, host);
        room.placeGomokStone(8, 5, guest);

        room.placeGomokStone(7, 6, host);
        room.placeGomokStone(8, 6, guest);

        // 마지막 착수로 5목 완성
        room.placeGomokStone(7, 7, host);
        Stone winner = room.getWinner(host);

        assertThat(winner).isEqualTo(Stone.BLACK);
    }

    @Test
    void 착수_후_5목이_아니면_EMPTY를_반환한다() {
        room.join(guest);
        room.startGomok(host);

        room.placeGomokStone(7, 3, host);
        room.placeGomokStone(8, 3, guest);
        room.placeGomokStone(7, 4, host);
        room.placeGomokStone(8, 4, guest);
        room.placeGomokStone(7, 5, host);
        room.placeGomokStone(8, 5, guest);
        room.placeGomokStone(7, 6, host);
        room.placeGomokStone(8, 6, guest);

        room.placeGomokStone(8, 8, host);
        Stone winner = room.getWinner(host);

        assertThat(winner).isEqualTo(Stone.EMPTY);
    }

    @Test
    void 상대방_차례에_착수하면_예외가_발생한다() {
        room.join(guest);
        room.startGomok(host);

        room.placeGomokStone(7, 7, host);

        assertThatThrownBy(() -> room.placeGomokStone(8, 8, host))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상대방의 차례입니다");
    }
}

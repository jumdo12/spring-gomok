package jumdo12.springgomok.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private User (String nickname, String userId, String password) {
        this.nickname = nickname;
        this.userId = userId;
        this.password = password;
    }

    public static User create (String nickname, String userId, String password) {
        return new User(nickname, userId, password);
    }
}

package jumdo12.springgomok.presentation;

import jakarta.servlet.http.HttpSession;
import jumdo12.springgomok.application.UserService;
import jumdo12.springgomok.domain.User;
import jumdo12.springgomok.presentation.dto.LoginRequest;
import jumdo12.springgomok.presentation.dto.SignUpRequest;
import jumdo12.springgomok.presentation.dto.LoginResponse;
import jumdo12.springgomok.presentation.session.SessionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SessionProvider sessionProvider;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> createUser(@RequestBody SignUpRequest signUpRequest) {
        User user = userService.createUser(signUpRequest.username(), signUpRequest.userId(), signUpRequest.password());

        LoginResponse loginResponse = LoginResponse.from(user);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpSession httpSession
    ) {
        User user = userService.findUser(loginRequest.userId(), loginRequest.password());

        sessionProvider.createUserSession(httpSession, user);
        LoginResponse loginResponse = LoginResponse.from(user);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession httpSession) {
        sessionProvider.removeSession(httpSession);

        return ResponseEntity.noContent().build();
    }
}

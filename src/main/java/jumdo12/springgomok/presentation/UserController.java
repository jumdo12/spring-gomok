package jumdo12.springgomok.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jumdo12.springgomok.application.UserService;
import jumdo12.springgomok.domain.User;
import jumdo12.springgomok.presentation.dto.LoginRequest;
import jumdo12.springgomok.presentation.dto.SignUpRequest;
import jumdo12.springgomok.presentation.dto.LoginResponse;
import jumdo12.springgomok.presentation.session.SessionProvider;
import lombok.RequiredArgsConstructor;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SessionProvider sessionProvider;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> createUser(@RequestBody SignUpRequest signUpRequest) {
        User user = userService.createUser(signUpRequest.username(), signUpRequest.userId(), signUpRequest.password());

        LoginResponse loginResponse = LoginResponse.from(user);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그인")
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

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession httpSession) {
        sessionProvider.removeSession(httpSession);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "현재 로그인한 사용자 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(@AuthUser LoginUser loginUser) {
        User user = userService.findUser(loginUser.id());
        LoginResponse loginResponse = LoginResponse.from(user);

        return ResponseEntity.ok(loginResponse);
    }
}

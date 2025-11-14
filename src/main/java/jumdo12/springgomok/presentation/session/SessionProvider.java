package jumdo12.springgomok.presentation.session;

import jakarta.servlet.http.HttpSession;
import jumdo12.springgomok.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessionProvider {

    private final static String SESSION_USER_KEY = "USER_ID";

    public void createUserSession(HttpSession httpSession, User user) {
        httpSession.setAttribute(SESSION_USER_KEY, user.getId());
    }

    public void removeSession(HttpSession httpSession) {
        httpSession.invalidate();
    }

    public Optional<Long> getUserIdSession(HttpSession httpSession) {
        if (httpSession == null) {
            return Optional.empty();
        }

        Long userId = (Long) httpSession.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            return Optional.empty();
        }

        return Optional.of(userId);
    }
}

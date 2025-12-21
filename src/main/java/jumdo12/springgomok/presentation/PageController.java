package jumdo12.springgomok.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/room-list")
    public String roomList() {
        return "room-list";
    }

    @GetMapping("/game")
    public String game() {
        return "game";
    }

    @GetMapping("/game-history")
    public String gameHistory() {
        return "game-history";
    }

    @GetMapping("/game-replay")
    public String gameReplay() {
        return "game-replay";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}

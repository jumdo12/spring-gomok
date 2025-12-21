package jumdo12.springgomok.presentation;

import jumdo12.springgomok.application.GomokHistoryService;
import jumdo12.springgomok.domain.PlaceResult;
import jumdo12.springgomok.presentation.resolver.AuthUser;
import jumdo12.springgomok.presentation.resolver.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games")
public class GomokResultController {

    private final GomokHistoryService gomokHistoryService;

    @GetMapping
    public List<String> myGomokGameId(@AuthUser LoginUser loginUser) {
        return gomokHistoryService.getUserGomokRecordId(loginUser);
    }

    @GetMapping("/{id}")
    public List<PlaceResult> getGomokResults(@PathVariable String id) {
        return gomokHistoryService.getGomokPlaceResults(id);
    }
}

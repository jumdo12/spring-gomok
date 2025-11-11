package jumdo12.springgomok.presentation.dto;

import jumdo12.springgomok.domain.User;

public record LoginResponse(
        Long id,
        String userId,
        String username
) {
    public static LoginResponse from(User user) {
        return new LoginResponse(user.getId(), user.getUserId(), user.getNickname());
    }
}

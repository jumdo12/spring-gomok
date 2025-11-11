package jumdo12.springgomok.presentation.dto;

public record SignUpRequest(
        String userId,
        String password,
        String username
) {

}

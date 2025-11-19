package jumdo12.springgomok.application;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.domain.User;
import jumdo12.springgomok.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username, String userId, String password) {
        String encodePassword = passwordEncoder.encode(password);

        return userRepository.save(User.create(username, userId, encodePassword));
    }

    public User findUser(String userId, String password) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return user;
    }

    public User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}

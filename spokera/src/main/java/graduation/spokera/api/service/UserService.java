package graduation.spokera.api.service;

import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 사용자 등록 (회원가입)
    public User registerUser(User user) {
        return userRepository.save(user);
    }


    // 사용자 위치 업데이트
    public User updateUserLocation(String username, Double latitude, Double longitude) {
        Optional<User> existingUserOpt = userRepository.findByNickname(username);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            existingUser.setLatitude(latitude);
            existingUser.setLongitude(longitude);

            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
    }

    // 사용자 삭제
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // 유저 위치 가져오기
    public UserLocationDTO getUserLocation(String username) {
        Optional<User> userOpt = userRepository.findByNickname(username);
        User user = userOpt.get();
        UserLocationDTO userLocationDTO = new UserLocationDTO();
        userLocationDTO.setUsername(user.getNickname());
        userLocationDTO.setLatitude(user.getLatitude());
        userLocationDTO.setLongitude(user.getLongitude());

        return userLocationDTO;
    }
}
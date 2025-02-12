package graduation.spokera.api.service;

import graduation.spokera.api.model.User;
import graduation.spokera.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 사용자 등록 (회원가입)
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    // 모든 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ID로 사용자 조회
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 사용자 정보 업데이트
    public User updateUser(Long userId, User updatedUser) {
        User user = getUserById(userId);
        user.setUsername(updatedUser.getUsername());
        user.setPassword(updatedUser.getPassword());
        user.setLatitude(updatedUser.getLatitude());
        user.setLongitude(updatedUser.getLongitude());
        user.setRating(updatedUser.getRating());
        return userRepository.save(user);
    }

    // 사용자 위치 업데이트
    public User updateUserLocation(String username, Double latitude, Double longitude) {
        Optional<User> existingUserOpt = userRepository.findByUsername(username);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // 🚀 위도, 경도만 업데이트
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
}
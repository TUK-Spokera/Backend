package graduation.spokera.api.controller;

import graduation.spokera.api.model.User;
import graduation.spokera.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 등록
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    // 위도/경도 업데이트
    @PutMapping("/{userId}/location")
    public ResponseEntity<User> updateUserLocation(
            @PathVariable String username,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        return ResponseEntity.ok(userService.updateUserLocation(username, latitude, longitude));
    }
}
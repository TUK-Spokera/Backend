package graduation.spokera.api.controller;

import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PutMapping("/location")
    public ResponseEntity<User> updateUserLocation(@RequestBody UserLocationDTO userLocation) {
        return ResponseEntity.ok(userService.updateUserLocation(
                userLocation.getUsername(),
                userLocation.getLatitude(),
                userLocation.getLongitude()
        ));
    }

    @GetMapping("/location")
    public ResponseEntity<UserLocationDTO> getUserLocation(@RequestParam String username) {
        return ResponseEntity.ok(userService.getUserLocation(username));
    }
}

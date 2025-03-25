package graduation.spokera.api.controller;

import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.util.LocationMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationQueryController {

    private final LocationMemoryStore locationStore;

    @GetMapping("/{userId}")
    public UserLocationDTO getUserLocation(@PathVariable String userId) {
        return locationStore.getLocation(userId);
    }

    @GetMapping("/all")
    public Collection<UserLocationDTO> getAllUserLocations() {
        return locationStore.getAllLocations().values();
    }
}

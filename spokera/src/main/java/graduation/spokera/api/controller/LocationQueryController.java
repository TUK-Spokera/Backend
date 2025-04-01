package graduation.spokera.api.controller;

import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.util.LocationMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

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

    @GetMapping("/team/{teamId}")
    public List<UserLocationDTO> getLocationsByTeam(@PathVariable String teamId) {
        return locationStore.getAllLocations().values().stream()
                .filter(loc -> teamId.equals(loc.getMatchId()))
                .toList();
    }

}

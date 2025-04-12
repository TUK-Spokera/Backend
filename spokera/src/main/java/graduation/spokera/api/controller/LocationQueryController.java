package graduation.spokera.api.controller;

import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.util.LocationMemoryStore;
import graduation.spokera.api.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationQueryController {

    private final LocationMemoryStore locationStore;
    private final MatchRepository matchRepository;

    @GetMapping("/{userId}")
    public UserLocationDTO getUserLocation(@PathVariable String userId) {
        return locationStore.getLocation(userId);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<UserLocationDTO>> getLocationsByMatch(@PathVariable String matchId) {
        boolean exists = matchRepository.existsById(Long.parseLong(matchId));
        if (!exists) {
            return ResponseEntity.badRequest().build();
        }

        List<UserLocationDTO> locations = locationStore.getAllLocations().values().stream()
                .filter(loc -> matchId.equals(loc.getMatchId()))
                .toList();

        return ResponseEntity.ok(locations);
    }
}

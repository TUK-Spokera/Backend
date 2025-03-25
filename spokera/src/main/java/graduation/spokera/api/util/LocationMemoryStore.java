package graduation.spokera.api.util;

import graduation.spokera.api.dto.user.UserLocationDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocationMemoryStore {

    private final Map<String, UserLocationDTO> locationMap = new ConcurrentHashMap<>();

    public void updateLocation(String userId, UserLocationDTO location) {
        locationMap.put(userId, location);
    }

    public UserLocationDTO getLocation(String userId) {
        return locationMap.get(userId);
    }

    public Map<String, UserLocationDTO> getAllLocations() {
        return locationMap;
    }
}

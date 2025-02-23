package graduation.spokera.api.controller;

import graduation.spokera.api.dto.facility.FacilityLocationResponse;
import graduation.spokera.api.service.FacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    @Autowired
    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public FacilityLocationResponse getFacilityLocation(@RequestParam String faciNm) {
        return facilityService.getFacilityLocation(faciNm);
    }
}

package graduation.spokera.api.controller;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.repository.FacilityRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityRepo facilityRepo;

    public FacilityController(FacilityRepo facilityRepo) {
        this.facilityRepo = facilityRepo;
    }

    @GetMapping
    public List<Facility> getFacilitiesByAddrCtpvNm(@RequestParam String addrCtpvNm) {
        return facilityRepo.findByAddrCtpvNm(addrCtpvNm);
    }
}

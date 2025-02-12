package graduation.spokera.api.controller;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.model.User;
import graduation.spokera.api.repository.FacilityRepo;
import graduation.spokera.api.service.FacilityService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityRepo facilityRepo;
    private final FacilityService facilityService;

    public FacilityController(FacilityRepo facilityRepo, FacilityService facilityService) {
        this.facilityRepo = facilityRepo;
        this.facilityService = facilityService;
    }

//    @GetMapping
//    public List<Facility> getFacilitiesByAddrCtpvNm(@RequestParam String addrCtpvNm) {
//        return facilityRepo.findTop100ByAddrCtpvNm(addrCtpvNm);
//    }

//    @PostMapping("/recommend")
//    public List<Facility> recommendFacilities(@RequestBody User user, @RequestParam String ftypeNm, @RequestParam int maxResults) {
//        return facilityService.recommendFacilities(new ArrayList<>((Collection) userLocation), ftypeNm, maxResults);
//    }

}

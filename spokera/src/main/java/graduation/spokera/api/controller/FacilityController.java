package graduation.spokera.api.controller;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.model.UserCoordinatesDto;
import graduation.spokera.api.repository.FacilityRepo;
import graduation.spokera.api.service.FacilityService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public List<Facility> getFacilitiesByAddrCtpvNm(@RequestParam String addrCtpvNm) {
        // 100개만 리턴하도록 바꿔봄
        // return facilityRepo.findByAddrCtpvNm(addrCtpvNm);
        return facilityRepo.findTop100ByAddrCtpvNm(addrCtpvNm);
    }

    /**
     * 추천 시설 목록 반환 ( 일단 두명의 좌표를 기반으로)
     * 요청예시 (json)
     * {
     *     "user1Lat": 37.5665,
     *     "user1Lot": 126.9780,
     *     "user2Lat": 37.5512,
     *     "user2Lot": 126.9885,
     *     "maxResults": 5
     * }
     *
     */
    @PostMapping("/recommend")
    public List<Facility> recommendFacilities(@RequestBody UserCoordinatesDto coordinates) {
        return facilityService.recommendFacilities(coordinates);
    }
}

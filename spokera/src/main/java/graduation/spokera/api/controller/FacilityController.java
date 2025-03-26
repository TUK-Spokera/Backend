package graduation.spokera.api.controller;

import graduation.spokera.api.dto.facility.FacilityRecommendResponseDTO;
import graduation.spokera.api.repository.FacilityRepository;
import graduation.spokera.api.dto.facility.FacilityLocationResponse;
import graduation.spokera.api.service.FacilityService;
import graduation.spokera.api.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;
    private final MatchService matchService;
    private final FacilityRepository facilityRepository;

    @GetMapping
    public FacilityLocationResponse getFacilityLocation(@RequestParam String faciNm) {
        return facilityService.getFacilityLocation(faciNm);
    }

    /**
     * 특정 매칭방의 사용자들을 기반으로 경기장 추천
     */
    @GetMapping("/recommend/{matchId}")
    public List<FacilityRecommendResponseDTO> recommendFacilitiesForMatch(@PathVariable Long matchId) {
        return matchService.recommendFacilitiesForMatch(matchId);
    }

}

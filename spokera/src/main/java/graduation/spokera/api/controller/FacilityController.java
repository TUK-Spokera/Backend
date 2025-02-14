package graduation.spokera.api.controller;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.model.User;
import graduation.spokera.api.repository.FacilityRepo;
import graduation.spokera.api.service.FacilityService;
import graduation.spokera.api.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityRepo facilityRepo;
    private final FacilityService facilityService;
    private final MatchService matchService;

    /**
     * 특정 매칭방의 사용자들을 기반으로 경기장 추천
     */
    @GetMapping("/recommend/{matchId}")
    public List<Facility> recommendFacilitiesForMatch(@PathVariable Long matchId) {
        return matchService.recommendFacilitiesForMatch(matchId);
    }

}

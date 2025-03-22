package graduation.spokera.api.service;

import graduation.spokera.api.domain.facility.*;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.facility.FacilityLocationResponse;
import graduation.spokera.api.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepo;

    public List<Facility> recommendFacilities(List<User> users, String ftypeNm, int maxResults) {

//        if (locations.size() < 2) {
//            throw new IllegalArgumentException("최소 2명의 위치 정보가 필요합니다.");
//        }
        // 여러 사용자의 평균 위도, 경도를 계산하여 중간 지점 찾기
        OptionalDouble avgLat = users.stream().mapToDouble(User::getLatitude).average();
        OptionalDouble avgLng = users.stream().mapToDouble(User::getLongitude).average();

        if (avgLat.isEmpty() || avgLng.isEmpty()) {
            throw new RuntimeException("위치 정보를 처리할 수 없습니다.");
        }

        double midLat = avgLat.getAsDouble();
        double midLng = avgLng.getAsDouble();

        // 특정 `FtypeNm`을 포함하는 시설 조회 (예: 배드민턴, 축구 등)
        List<Facility> allFacilities = facilityRepo.findByFtypeNmContaining(ftypeNm);

        // 거리 계산 후 가까운 경기장 추천 (maxResults 개수 제한)
        return allFacilities.stream()
                .filter(facility -> facility.getFaciLat() != null && facility.getFaciLot() != null)
                .sorted((f1, f2) -> Double.compare(
                        calculateDistance(midLat, midLng, f1.getFaciLat(), f1.getFaciLot()),
                        calculateDistance(midLat, midLng, f2.getFaciLat(), f2.getFaciLot())
                ))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    // 거리 계산 로직 (Haversine 공식)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }


    public FacilityLocationResponse getFacilityLocation(String faciNm) {
        Facility facility = findFacilityByName(faciNm);
        return new FacilityLocationResponse(facility.getFaciLat(), facility.getFaciLot(), 10); // altitude 고정값
    }

    private Facility findFacilityByName(String faciNm) {
        return facilityRepo.findByFaciNm(faciNm)
                .orElseThrow(() -> new IllegalArgumentException("해당 경기장을 찾을 수 없습니다: " + faciNm));
    }
}
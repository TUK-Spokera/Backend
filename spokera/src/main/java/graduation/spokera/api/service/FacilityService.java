package graduation.spokera.api.service;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.model.UserCoordinatesDto;
import graduation.spokera.api.repository.FacilityRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityService {

    private final FacilityRepo facilityRepo;

    public FacilityService(FacilityRepo facilityRepo) {
        this.facilityRepo = facilityRepo;
    }

    public List<Facility> recommendFacilities(UserCoordinatesDto coordinates) {
        // 두 사용자의 중간 지점 계산
        double midLat = (coordinates.getUser1Lat() + coordinates.getUser2Lat()) / 2;
        double midLng = (coordinates.getUser1Lot() + coordinates.getUser2Lot()) / 2;

        // 모든 시설 가져오기
        List<Facility> allFacilities = facilityRepo.findAll();

        // 거리 계산 및 정렬
        return allFacilities.stream()
                .filter(facility -> facility.getFaciLat() != null && facility.getFaciLot() != null) // null 필터링
                .sorted((f1, f2) -> {
                    double distance1 = calculateDistance(midLat, midLng, f1.getFaciLat(), f1.getFaciLot());
                    double distance2 = calculateDistance(midLat, midLng, f2.getFaciLat(), f2.getFaciLot());
                    return Double.compare(distance1, distance2);
                })
                .limit(coordinates.getMaxResults()) // 최대 결과 제한
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371; // 지구 반경 (단위: km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 거리 (단위: km)
    }


}

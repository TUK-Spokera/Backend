package graduation.spokera.api.repository;


import graduation.spokera.api.domain.facility.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Integer> {
    List<Facility> findByAddrCtpvNm(String addrCtpvNm);
    List<Facility> findTop100ByAddrCtpvNm(String addrCtpvNm);
    List<Facility> findByFtypeNmContaining(String ftypeNm);
    Optional<Facility> findByFaciNm(String faciNm);
}
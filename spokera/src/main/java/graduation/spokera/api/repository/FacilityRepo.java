package graduation.spokera.api.repository;


import graduation.spokera.api.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacilityRepo extends JpaRepository<Facility, Integer> {
    List<Facility> findByAddrCtpvNm(String addrCtpvNm);
}

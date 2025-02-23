package graduation.spokera.api.domain.facility;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "facility")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faci_id")
    private Integer id;

    @Column(name = "addr_ctpv_nm", length = 255)
    private String addrCtpvNm;

    @Column(name = "faci_daddr", length = 255)
    private String faciDaddr;

    @Column(name = "inout_gbn_nm", length = 255)
    private String inoutGbnNm;

    @Column(name = "faci_lat")
    private Double faciLat;

    @Column(name = "faci_lot")
    private Double faciLot;

    @Column(name = "faci_gfa")
    private Integer faciGfa;

    @Column(name = "ftype_nm", length = 255)
    private String ftypeNm;

    @Column(name = "cpb_nm", length = 100)
    private String cpbNm;

    @Column(name = "addr_emd_nm", length = 255)
    private String addrEmdNm;

    @Column(name = "addr_cpb_nm", length = 255)
    private String addrCpbNm;

    @Column(name = "faci_gb_nm", length = 255)
    private String faciGbNm;

    @Column(name = "faci_nm", length = 255)
    private String faciNm;

    @Column(name = "faci_road_addr", length = 255)
    private String faciRoadAddr;

    @Column(name = "fcob_nm", length = 255)
    private String fcobNm;

    @Column(name = "faci_addr", length = 255)
    private String faciAddr;

    @Column(name = "faci_cd", length = 255)
    private String faciCd;

    @Column(name = "faci_road_zip", length = 255)
    private String faciRoadZip;

    @Column(name = "faci_zip", length = 255)
    private String faciZip;
}

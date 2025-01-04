package graduation.spokera.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "facility")
@Getter
@Setter
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int faciId; // 시설 ID (Primary Key)

    private String addrCtpvNm; // 주소 - 시/도명
    private String faciDaddr; // 상세 주소
    private String inoutGbnNm; // 실내/실외 구분명
    private double faciLat; // 위도
    private double faciLot; // 경도
    private String faciZip; // 우편번호
    private int faciGfa; // 연면적
    private String ftypeNm; // 시설 유형명
    private String addrEmdNm; // 주소 - 읍/면/동명
    private String addrCpbNm; // 주소 - 추가명
    private String faciRoadZip; // 도로명 우편번호
    private String faciGbNm; // 시설 구분명
    private String faciNm; // 시설 이름
    private String faciRoadAddr; // 도로명 주소
    private String fcobNm; // 시설 회사명
    private String faciAddr; // 시설 전체 주소
    private String faciCd; // 시설 코드
}

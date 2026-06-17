package com.mycrewsoft.domain.reservation.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회의실 예약 조회 응답 DTO")
public class ReservationResponse {

    @Schema(description = "예약 ID", example = "1")
    private Long reservationId;

    @Schema(description = "회의실 ID", example = "3")
    private Long roomId;

    @Schema(description = "예약 제목", example = "주간 회의")
    private String title;

    @Schema(description = "예약자 ID", example = "1001")
    private Long reserverId;

    @Schema(description = "예약자 이름", example = "홍길동")
    private String reserverName;

    @Schema(description = "예약자 부서 코드", example = "D001")
    private String rsrvEmpDeptCd;

    @Schema(description = "예약자 직급 코드", example = "G001")
    private String rsrvEmpJobGrdCd;

    @Schema(description = "예약자 프로필 이미지 파일 ID", example = "10")
    private Long rsrvEmpPrflImgFileId;
    
    @Schema(description = "예약 시작 일시", example = "2026-06-05T09:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "예약 종료 일시", example = "2026-06-05T09:30:00")
    private LocalDateTime endDateTime;

    @Schema(description = "종일 예약 여부", example = "N")
    private String intgRsrvYn;
    
    @Schema(description = "내 예약 여부", example = "true")
    private Boolean mine;

    @Schema(description = "연동된 회의ID, 없으면 null")
    private Long mtngId;
}

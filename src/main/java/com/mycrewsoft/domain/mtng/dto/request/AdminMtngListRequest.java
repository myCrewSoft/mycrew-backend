package com.mycrewsoft.domain.mtng.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AdminMtngListRequest {

    @Schema(description = "검색 키워드 (회의명, 작성자명 대상)")
    private String keyword;

    @Schema(description = "회의 진행방식 코드 (온라인: MT001 / 오프라인: MT002 / 복합: MT003), 미입력 시 전체 조회")
    private String mtngTypeCd;

    @Schema(description = "화상회의 상태 코드 (예정: VC001 / 진행중: VC002 / 완료: VC003), 미입력 시 전체 조회")
    private String vconfSttus;

    @Schema(description = "회의록 상태 코드 (생성중: MM001 / 수정중: MM002 / 결재중: MM003 / 승인됨: MM004), 미입력 시 전체 조회")
    private String momSttusCd;

    @Schema(description = "조회 시작일시 (이 시각 이후 시작되는 회의)")
    private LocalDateTime beginDt;

    @Schema(description = "조회 종료일시 (이 시각 이전 시작되는 회의)")
    private LocalDateTime endDt;

    @Schema(description = "삭제된 회의 포함 여부 (true: 포함 / false: 제외), 기본값 false")
    private boolean includeDeleted;

    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    @Schema(description = "페이지 번호 (1부터 시작)", defaultValue = "1")
    private int page = 1;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Schema(description = "페이지당 조회 건수", defaultValue = "10")
    private int size = 10;
}
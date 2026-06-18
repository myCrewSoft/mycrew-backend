package com.mycrewsoft.domain.dashboard.dto.response.widget;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 대시보드 - 사원 근태 현황 위젯 응답 (지각/조퇴/결근 등 특이사항 사원 목록)")
public class AdminAttendanceWidgetResponse {

    @Schema(description = "특이사항 사원 수", example = "3")
    private int count;

    @Schema(description = "특이사항 사원 목록 (최대 5명)")
    private List<AttendanceItem> employees;

    @Getter
    @Builder
    @Schema(description = "근태 특이사항 사원 항목")
    public static class AttendanceItem {

        @Schema(description = "사원 ID", example = "1001")
        private Long empId;

        @Schema(description = "사원명", example = "임원호")
        private String empNm;

        @Schema(description = "부서명", example = "개발팀")
        private String deptNm;

        @Schema(description = "직급명", example = "대리")
        private String jbpsNm;

        @Schema(description = "근태 상태 코드 (LATE:지각, EARLY:조퇴, ABSENT:결근)", example = "LATE")
        private String status;

        @Schema(description = "근태 상태명", example = "지각")
        private String statusName;

        @Schema(description = "출근 시각 (HH:mm), 미출근이면 null", example = "09:32")
        private String checkInAt;

        @Schema(description = "퇴근 시각 (HH:mm), 퇴근 전이면 null", example = "17:40")
        private String checkOutAt;

        @Schema(description = "지각 시간(분)", example = "32")
        private Integer lateMin;

        @Schema(description = "조퇴 시간(분)", example = "20")
        private Integer earlyLeaveMin;
    }
}

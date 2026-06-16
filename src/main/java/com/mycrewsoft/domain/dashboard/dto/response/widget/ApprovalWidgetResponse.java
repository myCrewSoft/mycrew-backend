package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "전자결재 위젯 응답")
public class ApprovalWidgetResponse {

    @Schema(description = "결재 대기 건수")
    private int pendingCount;

    @Schema(description = "결재 대기 문서 목록")
    private List<ApprovalItem> documents;

    @Getter
    @Builder
    public static class ApprovalItem {

        @Schema(description = "기안서 ID")
        private Long id;

        @Schema(description = "기안서 제목")
        private String title;

        @Schema(description = "기안자 이름")
        private String requesterName;

        @Schema(description = "기안 일시")
        private String requestedAt;
        
        @Schema(description = "결재 희망 일시")
        private String dueDate;

        @Schema(description = "D-day (오늘 기준, 음수면 초과)")
        private String dDay;
    }
}
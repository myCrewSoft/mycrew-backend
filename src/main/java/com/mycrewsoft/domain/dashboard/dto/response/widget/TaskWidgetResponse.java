package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "업무 위젯 응답")
public class TaskWidgetResponse {

    @Schema(description = "업무 목록")
    private List<TaskItem> tasks;

    @Getter
    @Builder
    public static class TaskItem {

        @Schema(description = "업무 ID")
        private Long id;

        @Schema(description = "업무 제목")
        private String title;

        @Schema(description = "마감일 (yyyy-MM-dd)")
        private String dueDate;

        @Schema(description = "업무 상태")
        private String status;
        
        @Schema(description = "프로젝트 ID")
        private Long projId;
    }
}
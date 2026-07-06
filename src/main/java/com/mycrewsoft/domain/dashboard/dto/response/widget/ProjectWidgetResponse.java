package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "프로젝트 진행률 위젯 응답")
public class ProjectWidgetResponse {

    @Schema(description = "프로젝트 목록")
    private List<ProjectItem> projects;

    @Getter
    @Builder
    public static class ProjectItem {

        @Schema(description = "프로젝트 ID")
        private Long id;

        @Schema(description = "프로젝트 이름")
        private String name;

        @Schema(description = "진행률 (0~100)")
        private int progressRate;

        @Schema(description = "마감일 (yyyy-MM-dd)")
        private String dueDate;
    }
}
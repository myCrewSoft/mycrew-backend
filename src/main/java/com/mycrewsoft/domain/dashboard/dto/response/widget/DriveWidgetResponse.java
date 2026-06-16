package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "드라이브 위젯 응답")
public class DriveWidgetResponse {

    @Schema(description = "즐겨찾기 파일 목록")
    private List<DriveItem> files;

    @Getter
    @Builder
    public static class DriveItem {

        @Schema(description = "파일 ID")
        private Long id;

        @Schema(description = "파일명")
        private String fileName;

        @Schema(description = "파일 타입 (pdf/docx/xlsx 등)")
        private String fileType;

        @Schema(description = "수정일시")
        private String updatedAt;

        @Schema(description = "소유자 이름")
        private String ownerName;
    }
}
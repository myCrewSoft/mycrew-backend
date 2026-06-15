package com.mycrewsoft.domain.mail.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Gmail 동기화 응답")
public class MailSyncResponse {

    @Schema(description = "Gmail에서 가져온 메시지 수", example = "10")
    private int syncedCount;

    @Schema(description = "신규 저장한 메시지 수", example = "7")
    private int insertedCount;

    @Schema(description = "기존 메시지를 갱신한 수", example = "3")
    private int updatedCount;

    @Schema(description = "처리하지 않고 건너뛴 메시지 수", example = "0")
    private int skippedCount;

    @Schema(description = "최신 Gmail historyId", example = "123456")
    private String latestHistoryId;

    @Schema(description = "동기화 완료 시각")
    private LocalDateTime syncedAt;
}

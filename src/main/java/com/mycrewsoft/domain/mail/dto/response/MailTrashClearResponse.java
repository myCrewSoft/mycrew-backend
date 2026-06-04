package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "휴지통 비우기 응답")
public class MailTrashClearResponse {

    @Schema(description = "영구 삭제된 메일 수", example = "3")
    private int deletedCount;
}

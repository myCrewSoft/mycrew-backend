package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class VideoConfVO {

    @Setter
    private Long vconfId; // 화상회의ID

    private Long mtngId; // 회의ID (논리FK)

    @Setter
    private String roomNm; // LiveKit room명

    private String vconfNm; // 화상회의명

    private Long crtrId; // 작성자ID

    private LocalDateTime creatDt; // 생성일시
}
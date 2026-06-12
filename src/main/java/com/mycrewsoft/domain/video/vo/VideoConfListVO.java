package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoConfListVO {

    private Long vconfId;
    private String vconfNm;
    private String roomNm;
    private Long crtrId;
    private String crtrNm;
    private String confSttusCd;
    private String momSttusCd;
    private LocalDateTime beginDt;
    private LocalDateTime endDt;
    private LocalDateTime creatDt;

    // has many
    private List<VideoPtcptDetailVO> videoPtcpt;
}
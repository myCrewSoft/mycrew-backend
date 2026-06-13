package com.mycrewsoft.domain.notification.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationQueryVO {

    private Long alrmRcvrId;
    private Long alrmId;
    private String alrmTypeCd;
    private String alrmTtln;
    private String alrmCn;
    private LocalDateTime alrmSndngDt;
    private LocalDateTime alrmCfmtnDt;
}
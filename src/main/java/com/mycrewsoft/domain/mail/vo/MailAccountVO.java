package com.mycrewsoft.domain.mail.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailAccountVO {

    private Long empId;
    private String providerCd;
    private String emailAddr;
    private String googleSubId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExprDt;
    private LocalDateTime syncLastDt;
    private String useYn;
    private LocalDateTime frstRegDt;
    private LocalDateTime lastMdfcnDt;
}

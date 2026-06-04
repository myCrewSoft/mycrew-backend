package com.mycrewsoft.domain.mail.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailMessageRow {

    private Long mailId;
    private Long empId;
    private String externalMessageId;
    private String threadId;
    private String messageIdHeader;
    private String subject;
    private String content;
    private String snippet;
    private String fromEmail;
    private String toSummary;
    private LocalDateTime sentAt;
    private LocalDateTime internalDate;
    private String draftYn;
    private String bodySyncYn;
    private String delYn;
}

package com.mycrewsoft.domain.mail.gmail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GmailSyncedMessage {

    private String externalMessageId;
    private String threadId;
    private String historyId;
    private String messageIdHeader;
    private String subject;
    private String content;
    private String snippet;
    private String fromEmail;
    private String replyToEmail;
    private LocalDateTime sentAt;
    private LocalDateTime internalDate;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<GmailSyncedAttachment> attachments = new ArrayList<>();
}

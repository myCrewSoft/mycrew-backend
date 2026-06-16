package com.mycrewsoft.domain.mail.gmail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GmailSendCommand {

    private String fromEmail;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String subject;
    private String content;
    private List<MultipartFile> attachments = new ArrayList<>();
    private String inReplyTo;
    private String references;
    private String threadId;
}

package com.mycrewsoft.domain.mail.gmail;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailSendResult {

    private String externalMessageId;
    private String threadId;
    private LocalDateTime sentAt;
}

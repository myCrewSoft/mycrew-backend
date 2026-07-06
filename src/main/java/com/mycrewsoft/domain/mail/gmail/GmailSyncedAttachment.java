package com.mycrewsoft.domain.mail.gmail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncedAttachment {

    private String originalFileName;
    private String contentType;
    private byte[] content;
}

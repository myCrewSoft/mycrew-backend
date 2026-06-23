package com.mycrewsoft.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class MailSearchDetails implements SearchResultDetails {
    private String senderName;
    private String senderAddress;
    private LocalDateTime receivedAt;
    private Boolean read;
    private Boolean hasAttachment;
    private String bodyPreview;
}

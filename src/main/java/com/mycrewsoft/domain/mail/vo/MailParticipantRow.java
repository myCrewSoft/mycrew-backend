package com.mycrewsoft.domain.mail.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailParticipantRow {

    private Long participantId;
    private Long mailId;
    private Long empId;
    private String email;
    private String type;
}

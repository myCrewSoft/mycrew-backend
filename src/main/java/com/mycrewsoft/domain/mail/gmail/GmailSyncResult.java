package com.mycrewsoft.domain.mail.gmail;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GmailSyncResult {

    private List<GmailSyncedMessage> messages = new ArrayList<>();
    private String latestHistoryId;
}

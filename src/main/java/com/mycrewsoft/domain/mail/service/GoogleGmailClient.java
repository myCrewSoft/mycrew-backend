package com.mycrewsoft.domain.mail.service;

import java.util.List;

import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSendCommand;
import com.mycrewsoft.domain.mail.gmail.GmailSendResult;
import com.mycrewsoft.domain.mail.gmail.GmailSyncResult;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

public interface GoogleGmailClient {

    GmailSendResult sendMessage(MailAccountVO account, GmailSendCommand command);

    GmailMessageContent getMessage(MailAccountVO account, String externalMessageId);

    void markRead(MailAccountVO account, String externalMessageId);

    void markRead(MailAccountVO account, List<String> externalMessageIds);

    void markUnread(MailAccountVO account, String externalMessageId);

    void markUnread(MailAccountVO account, List<String> externalMessageIds);

    void updateImportant(MailAccountVO account, String externalMessageId, boolean important);

    void updateImportant(MailAccountVO account, List<String> externalMessageIds, boolean important);

    void trashMessage(MailAccountVO account, String externalMessageId);

    void trashMessages(MailAccountVO account, List<String> externalMessageIds);

    void untrashMessage(MailAccountVO account, String externalMessageId);

    void deleteMessage(MailAccountVO account, String externalMessageId);

    void deleteMessages(MailAccountVO account, List<String> externalMessageIds);

    GmailSyncResult syncMessages(MailAccountVO account, String startHistoryId, int maxResults);
}

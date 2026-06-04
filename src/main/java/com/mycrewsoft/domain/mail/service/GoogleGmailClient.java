package com.mycrewsoft.domain.mail.service;

import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSendCommand;
import com.mycrewsoft.domain.mail.gmail.GmailSendResult;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

public interface GoogleGmailClient {

    GmailSendResult sendMessage(MailAccountVO account, GmailSendCommand command);

    GmailMessageContent getMessage(MailAccountVO account, String externalMessageId);

    void markRead(MailAccountVO account, String externalMessageId);

    void updateImportant(MailAccountVO account, String externalMessageId, boolean important);

    void trashMessage(MailAccountVO account, String externalMessageId);

    void untrashMessage(MailAccountVO account, String externalMessageId);

    void deleteMessage(MailAccountVO account, String externalMessageId);
}

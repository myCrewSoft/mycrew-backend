package com.mycrewsoft.domain.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSyncResponse;
import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSyncResult;
import com.mycrewsoft.domain.mail.gmail.GmailSyncedMessage;
import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;
import com.mycrewsoft.domain.mail.vo.MailMessageRow;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.authz.ScopedPermission;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    private static final Long EMP_ID = 1L;

    @Mock
    private MailMapper mailMapper;

    @Mock
    private GoogleGmailClient googleGmailClient;

    @Mock
    private FileService fileService;

    private MailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MailServiceImpl(new AuthorizationService(), mailMapper, googleGmailClient, fileService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void markReadCallsGmailBeforeRemovingUnreadLabel() {
        authenticate(PermissionCode.MAIL_READ);
        MailAccountVO account = account("https://www.googleapis.com/auth/gmail.modify");
        MailMessageRow row = mailRow();
        when(mailMapper.selectActiveMailAccount(EMP_ID)).thenReturn(account);
        when(mailMapper.selectMailRow(EMP_ID, 10L)).thenReturn(row);

        MailMutationResponse response = service.markRead(10L);

        assertThat(response.getStatus()).isEqualTo("READ");
        InOrder inOrder = inOrder(googleGmailClient, mailMapper);
        inOrder.verify(googleGmailClient).markRead(account, "gmail-10");
        inOrder.verify(mailMapper).deleteLabelMapByType(EMP_ID, 10L, "UNREAD");
    }

    @Test
    void markReadFailsBeforeGmailCallWhenModifyScopeIsMissing() {
        authenticate(PermissionCode.MAIL_READ);
        MailAccountVO account = account("https://www.googleapis.com/auth/gmail.readonly");
        when(mailMapper.selectActiveMailAccount(EMP_ID)).thenReturn(account);

        assertThatThrownBy(() -> service.markRead(10L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MAIL_SCOPE_REQUIRED);

        verify(mailMapper, never()).selectMailRow(EMP_ID, 10L);
        verifyNoInteractions(googleGmailClient);
    }

    @Test
    void getMailFetchesBodyFromGmailWhenBodyIsNotSynced() {
        authenticate(PermissionCode.MAIL_READ);
        MailAccountVO account = account("https://www.googleapis.com/auth/gmail.readonly");
        MailMessageRow row = mailRow();
        row.setBodySyncYn("N");
        MailDetailResponse detail = new MailDetailResponse();
        detail.setMailId(10L);

        when(mailMapper.selectActiveMailAccount(EMP_ID)).thenReturn(account);
        when(mailMapper.selectMailRow(EMP_ID, 10L)).thenReturn(row);
        when(googleGmailClient.getMessage(account, "gmail-10")).thenReturn(new GmailMessageContent("<p>본문</p>", "본문"));
        when(mailMapper.selectMailDetail(EMP_ID, 10L)).thenReturn(detail);
        when(mailMapper.selectParticipants(EMP_ID, 10L)).thenReturn(List.of());
        when(mailMapper.selectAttachments(EMP_ID, 10L)).thenReturn(List.of());
        when(mailMapper.selectLabelTypes(EMP_ID, 10L)).thenReturn(List.of("INBOX"));

        MailDetailResponse response = service.getMail(10L);

        assertThat(response.getLabels()).containsExactly("INBOX");
        InOrder inOrder = inOrder(googleGmailClient, mailMapper);
        inOrder.verify(googleGmailClient).getMessage(account, "gmail-10");
        inOrder.verify(mailMapper).updateMailBody(EMP_ID, 10L, "<p>본문</p>", "본문");
    }

    @Test
    void syncMailsInsertsNewGmailMessageAndUpdatesHistoryId() {
        authenticate(PermissionCode.MAIL_READ);
        MailAccountVO account = account("https://www.googleapis.com/auth/gmail.readonly");
        account.setGoogleHistoryId(null);
        GmailSyncedMessage message = syncedMessage();
        GmailSyncResult syncResult = new GmailSyncResult();
        syncResult.setMessages(List.of(message));
        syncResult.setLatestHistoryId("history-11");

        when(mailMapper.selectActiveMailAccount(EMP_ID)).thenReturn(account);
        when(googleGmailClient.syncMessages(account, null, 50)).thenReturn(syncResult);
        when(mailMapper.selectMailIdByExternalMessageId(EMP_ID, "gmail-new")).thenReturn(null);
        when(mailMapper.selectNextMailMessageId()).thenReturn(100L);
        when(mailMapper.selectNextMailLabelId()).thenReturn(1L, 2L, 3L, 4L, 5L);
        when(mailMapper.selectLabelIdByType(EMP_ID, "INBOX")).thenReturn(1L);
        when(mailMapper.selectLabelIdByType(EMP_ID, "UNREAD")).thenReturn(4L);
        when(mailMapper.existsLabelMap(any(), any(), any())).thenReturn(0);
        when(mailMapper.selectNextMailLabelMapId()).thenReturn(10L, 11L);
        when(mailMapper.selectNextMailParticipantId()).thenReturn(20L, 21L);

        MailSyncResponse response = service.syncMails(50);

        assertThat(response.getSyncedCount()).isEqualTo(1);
        assertThat(response.getInsertedCount()).isEqualTo(1);
        assertThat(response.getUpdatedCount()).isZero();
        assertThat(response.getLatestHistoryId()).isEqualTo("history-11");
        verify(mailMapper).insertMailMessage(any(MailMessageRow.class));
        verify(mailMapper, times(2)).insertParticipant(any());
        verify(mailMapper).updateMailAccountSyncState(EMP_ID, "history-11");
    }

    @Test
    void syncMailsTreatsNullMessagesAsEmptySync() {
        authenticate(PermissionCode.MAIL_READ);
        MailAccountVO account = account("https://www.googleapis.com/auth/gmail.readonly");
        GmailSyncResult syncResult = new GmailSyncResult();
        syncResult.setMessages(null);
        syncResult.setLatestHistoryId("history-empty");

        when(mailMapper.selectActiveMailAccount(EMP_ID)).thenReturn(account);
        when(googleGmailClient.syncMessages(account, null, 50)).thenReturn(syncResult);
        when(mailMapper.selectNextMailLabelId()).thenReturn(1L, 2L, 3L, 4L, 5L);

        MailSyncResponse response = service.syncMails(50);

        assertThat(response.getSyncedCount()).isZero();
        assertThat(response.getInsertedCount()).isZero();
        assertThat(response.getUpdatedCount()).isZero();
        assertThat(response.getLatestHistoryId()).isEqualTo("history-empty");
        verify(mailMapper).updateMailAccountSyncState(EMP_ID, "history-empty");
    }

    private void authenticate(PermissionCode permissionCode) {
        AuthorizationUserDetails principal = new AuthorizationUserDetails(
                EMP_ID,
                "user",
                "",
                true,
                1,
                false,
                List.of(),
                List.of(ScopedPermission.of(permissionCode.getCode(), 1L, "role", ScopeType.GLOBAL, null)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private MailAccountVO account(String scopes) {
        MailAccountVO account = new MailAccountVO();
        account.setEmpId(EMP_ID);
        account.setEmailAddr("user@example.com");
        account.setAccessToken("access-token");
        account.setScopeCn(scopes);
        return account;
    }

    private MailMessageRow mailRow() {
        MailMessageRow row = new MailMessageRow();
        row.setEmpId(EMP_ID);
        row.setMailId(10L);
        row.setExternalMessageId("gmail-10");
        row.setBodySyncYn("Y");
        row.setDelYn("N");
        return row;
    }
    private GmailSyncedMessage syncedMessage() {
        GmailSyncedMessage message = new GmailSyncedMessage();
        message.setExternalMessageId("gmail-new");
        message.setThreadId("thread-new");
        message.setHistoryId("history-10");
        message.setMessageIdHeader("<message@example.com>");
        message.setSubject("subject");
        message.setContent("<p>content</p>");
        message.setSnippet("content");
        message.setFromEmail("sender@example.com");
        message.setTo(List.of("user@example.com"));
        message.setLabels(List.of("INBOX", "UNREAD"));
        message.setSentAt(java.time.LocalDateTime.now());
        message.setInternalDate(message.getSentAt());
        return message;
    }
}

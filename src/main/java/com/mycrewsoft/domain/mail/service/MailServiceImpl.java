package com.mycrewsoft.domain.mail.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.request.MailBulkRequest;
import com.mycrewsoft.domain.mail.dto.request.MailDraftRequest;
import com.mycrewsoft.domain.mail.dto.request.MailLabelRequest;
import com.mycrewsoft.domain.mail.dto.request.MailSendRequest;
import com.mycrewsoft.domain.mail.dto.response.MailAccountStatusResponse;
import com.mycrewsoft.domain.mail.dto.response.MailAttachmentDownload;
import com.mycrewsoft.domain.mail.dto.response.MailAttachmentResponse;
import com.mycrewsoft.domain.mail.dto.response.MailBulkResponse;
import com.mycrewsoft.domain.mail.dto.response.MailLabelResponse;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailParticipantResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSyncResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;
import com.mycrewsoft.domain.mail.dto.response.MailUnreadCountResponse;
import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSendCommand;
import com.mycrewsoft.domain.mail.gmail.GmailSendResult;
import com.mycrewsoft.domain.mail.gmail.GmailSyncResult;
import com.mycrewsoft.domain.mail.gmail.GmailSyncedAttachment;
import com.mycrewsoft.domain.mail.gmail.GmailSyncedMessage;
import com.mycrewsoft.domain.mail.event.MailReceivedEvent;
import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;
import com.mycrewsoft.domain.mail.vo.MailMessageRow;
import com.mycrewsoft.domain.mail.vo.MailParticipantRow;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private static final String SCOPE_GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly";
    private static final String SCOPE_GMAIL_SEND = "https://www.googleapis.com/auth/gmail.send";
    private static final String SCOPE_GMAIL_MODIFY = "https://www.googleapis.com/auth/gmail.modify";
    private static final String SCOPE_GMAIL_FULL_ACCESS = "https://mail.google.com/";
    private static final String MAIL_ATTACHMENT_BIZ_CD = "05";
    private static final String TOKEN_STATUS_ACTIVE = "ACTIVE";
    private static final String TOKEN_STATUS_INVALID = "INVALID";
    private static final String TOKEN_STATUS_REVOKED = "REVOKED";
    private static final String ACCOUNT_STATUS_NONE = "NONE";
    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    private static final String ACCOUNT_STATUS_TOKEN_INVALID = "TOKEN_INVALID";
    private static final String CONTENT_RENDER_MODE_SANDBOX_IFRAME = "SANDBOX_IFRAME";
    private static final Set<String> MAILBOX_TYPES = Set.of("inbox", "sent", "all", "self", "tome", "important", "unread", "draft");
    private static final Set<String> BULK_ACTIONS = Set.of("read", "unread", "trash", "important");
    private static final List<String> SYSTEM_LABELS = List.of("INBOX", "SENT", "TRASH", "UNREAD", "IMPORTANT");
    private static final int WIDGET_MAIL_LIMIT = 5;	// 위젯 메일 개수

    private final AuthorizationService authorizationService;
    private final MailMapper mailMapper;
    private final GoogleGmailClient googleGmailClient;
    private final FileService fileService;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public MailAccountStatusResponse getAccountStatus() {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = mailMapper.selectActiveMailAccount(empId);
        if (account == null) {
            return new MailAccountStatusResponse(false, ACCOUNT_STATUS_NONE, null, null, false);
        }
        String tokenStatus = normalizeTokenStatus(account);
        boolean reconnectRequired = isReconnectRequired(account);
        return new MailAccountStatusResponse(
                true,
                reconnectRequired ? ACCOUNT_STATUS_TOKEN_INVALID : ACCOUNT_STATUS_ACTIVE,
                account.getEmailAddr(),
                tokenStatus,
                reconnectRequired);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MailSummaryResponse> getMails(String type, String keyword, Pageable pageable) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        String normalizedType = normalizeType(type);
        long total = mailMapper.countMails(
                empId,
                normalizedType,
                normalizeKeyword(keyword),
                account.getEmailAddr());
        List<MailSummaryResponse> mails = mailMapper.selectMails(
                empId,
                normalizedType,
                normalizeKeyword(keyword),
                account.getEmailAddr(),
                pageable.getOffset(),
                pageable.getPageSize());
        fillLabels(empId, mails);
        return new PageImpl<>(mails, pageable, total);
    }

    @Override
    public MailSendResponse sendMail(MailSendRequest request, List<MultipartFile> attachments) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_SEND, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_SEND);

        List<String> to = normalizeEmails(request.getTo());
        if (to.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<String> cc = normalizeEmails(request.getCc());
        List<String> bcc = normalizeEmails(request.getBcc());
        List<MultipartFile> uploadFiles = attachments == null ? List.of() : attachments;

        String subject = request.getSubject().trim();
        String content = request.getContent();

        GmailSendCommand command = new GmailSendCommand();
        command.setFromEmail(account.getEmailAddr());
        command.setTo(to);
        command.setCc(cc);
        command.setBcc(bcc);
        command.setSubject(subject);
        command.setContent(content);
        command.setAttachments(uploadFiles);

        // 답장/회신 대상이 있으면 동일 스레드로 연결 (In-Reply-To/References + threadId)
        if (request.getInReplyToMailId() != null) {
            MailMessageRow original = mailMapper.selectMailRow(empId, request.getInReplyToMailId());
            if (original != null) {
                command.setThreadId(original.getThreadId());
                if (original.getMessageIdHeader() != null && !original.getMessageIdHeader().isBlank()) {
                    command.setInReplyTo(original.getMessageIdHeader());
                    command.setReferences(original.getMessageIdHeader());
                }
            }
        }

        GmailSendResult result = googleGmailClient.sendMessage(account, command);
        if (result == null || result.getExternalMessageId() == null) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }

        LocalDateTime sentAt = result.getSentAt() == null ? LocalDateTime.now() : result.getSentAt();
        return transactionTemplate.execute(status -> persistSentMail(
                empId,
                account,
                subject,
                content,
                to,
                cc,
                bcc,
                uploadFiles,
                result,
                sentAt));
    }

    @Override
    public MailSendResponse sendDraft(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_SEND, empId);
        if (mailId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_SEND);

        MailMessageRow draft = mailMapper.selectMailRow(empId, mailId);
        if (draft == null || !"Y".equals(draft.getDraftYn())) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }

        List<MailParticipantResponse> participants = mailMapper.selectParticipants(empId, mailId);
        List<String> to = emailsByType(participants, "TO");
        if (to.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<String> cc = emailsByType(participants, "CC");
        List<String> bcc = emailsByType(participants, "BCC");
        String subject = draft.getSubject() == null ? "" : draft.getSubject().trim();
        String content = draft.getContent() == null ? "" : draft.getContent();
        if (subject.isBlank() || content.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        GmailSendCommand command = new GmailSendCommand();
        command.setFromEmail(account.getEmailAddr());
        command.setTo(to);
        command.setCc(cc);
        command.setBcc(bcc);
        command.setSubject(subject);
        command.setContent(content);
        command.setAttachments(List.of());

        GmailSendResult result = googleGmailClient.sendMessage(account, command);
        if (result == null || result.getExternalMessageId() == null) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }

        LocalDateTime sentAt = result.getSentAt() == null ? LocalDateTime.now() : result.getSentAt();
        return transactionTemplate.execute(status -> {
            MailSendResponse response = persistSentMail(
                    empId,
                    account,
                    subject,
                    content,
                    to,
                    cc,
                    bcc,
                    List.of(),
                    result,
                    sentAt);
            deleteDraftRow(empId, mailId);
            return response;
        });
    }

    private MailSendResponse persistSentMail(Long empId, MailAccountVO account, String subject, String content,
            List<String> to, List<String> cc, List<String> bcc, List<MultipartFile> uploadFiles,
            GmailSendResult result, LocalDateTime sentAt) {
        ensureSystemLabels(empId);

        Long mailId = mailMapper.selectNextMailMessageId();
        MailMessageRow row = new MailMessageRow();
        row.setMailId(mailId);
        row.setEmpId(empId);
        row.setExternalMessageId(result.getExternalMessageId());
        row.setThreadId(result.getThreadId());
        row.setSubject(subject);
        row.setContent(content);
        row.setSnippet(buildSnippet(content));
        row.setFromEmail(account.getEmailAddr());
        row.setToSummary(buildToSummary(to));
        row.setSentAt(sentAt);
        row.setInternalDate(sentAt);
        row.setDraftYn("N");
        row.setBodySyncYn("Y");
        row.setDelYn("N");
        mailMapper.insertMailMessage(row);

        insertParticipant(empId, mailId, account.getEmailAddr(), "FROM");
        to.forEach(email -> insertParticipant(empId, mailId, email, "TO"));
        cc.forEach(email -> insertParticipant(empId, mailId, email, "CC"));
        bcc.forEach(email -> insertParticipant(empId, mailId, email, "BCC"));

        addLabel(empId, mailId, "SENT");
        for (MultipartFile file : uploadFiles) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            FileUploadRequestDto uploadRequest = new FileUploadRequestDto();
            uploadRequest.setFile(file);
            uploadRequest.setFileCn("메일 첨부파일");
            Long attachmentId = fileService.upload(uploadRequest, MAIL_ATTACHMENT_BIZ_CD);
            mailMapper.insertAttachment(attachmentId, empId, mailId);
        }

        return new MailSendResponse(mailId, result.getExternalMessageId(), result.getThreadId(), sentAt);
    }

    @Override
    public MailDetailResponse getMail(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        MailMessageRow row = loadMailRow(empId, mailId);
        if ("N".equals(row.getBodySyncYn())) {
            GmailMessageContent content = googleGmailClient.getMessage(account, row.getExternalMessageId());
            if (content == null) {
                throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
            }
            transactionTemplate.executeWithoutResult(status ->
                    mailMapper.updateMailBody(empId, mailId, content.getContent(), content.getSnippet()));
        }

        MailDetailResponse detail = mailMapper.selectMailDetail(empId, mailId);
        if (detail == null) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
        detail.setParticipants(mailMapper.selectParticipants(empId, mailId));
        detail.setAttachments(mailMapper.selectAttachments(empId, mailId));
        detail.setLabels(mailMapper.selectLabelTypes(empId, mailId));
        applyRenderPolicy(detail);
        return detail;
    }

    @Override
    public MailMutationResponse moveToTrash(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_DELETE, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.trashMessage(account, row.getExternalMessageId());
        transactionTemplate.executeWithoutResult(status -> {
            ensureSystemLabels(empId);
            addLabel(empId, mailId, "TRASH");
        });
        return new MailMutationResponse(mailId, "TRASHED");
    }

    @Override
    public MailMutationResponse markRead(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.markRead(account, row.getExternalMessageId());
        transactionTemplate.executeWithoutResult(status ->
                mailMapper.deleteLabelMapByType(empId, mailId, "UNREAD"));
        return new MailMutationResponse(mailId, "READ");
    }

    @Override
    public MailMutationResponse markUnread(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.markUnread(account, row.getExternalMessageId());
        transactionTemplate.executeWithoutResult(status -> {
            ensureSystemLabels(empId);
            addLabel(empId, mailId, "UNREAD");
        });
        return new MailMutationResponse(mailId, "UNREAD");
    }

    @Override
    public MailMutationResponse updateImportant(Long mailId, MailImportantUpdateRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);
        if (request == null || request.getImportant() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        googleGmailClient.updateImportant(account, row.getExternalMessageId(), request.getImportant());
        transactionTemplate.executeWithoutResult(status -> {
            ensureSystemLabels(empId);
            if (Boolean.TRUE.equals(request.getImportant())) {
                addLabel(empId, mailId, "IMPORTANT");
            } else {
                mailMapper.deleteLabelMapByType(empId, mailId, "IMPORTANT");
            }
        });
        return new MailMutationResponse(mailId, "IMPORTANT_UPDATED");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MailSummaryResponse> getTrash(Pageable pageable) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        long total = mailMapper.countTrash(empId);
        List<MailSummaryResponse> mails = mailMapper.selectTrash(empId, pageable.getOffset(), pageable.getPageSize());
        fillLabels(empId, mails);
        return new PageImpl<>(mails, pageable, total);
    }

    @Override
    public MailTrashClearResponse clearTrash() {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_DELETE, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_FULL_ACCESS);

        List<MailMessageRow> trashRows = mailMapper.selectTrashRows(empId);
        log.info("Mail trash clear target loaded. empId={}, targetCount={}", empId, trashRows.size());
        if (trashRows.isEmpty()) {
            return new MailTrashClearResponse(0);
        }

        for (MailMessageRow row : trashRows) {
            googleGmailClient.deleteMessage(account, row.getExternalMessageId());
        }
        transactionTemplate.executeWithoutResult(status ->
                mailMapper.markMessagesDeleted(empId, trashRows.stream().map(MailMessageRow::getMailId).toList()));
        return new MailTrashClearResponse(trashRows.size());
    }

    @Override
    public MailMutationResponse restore(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.untrashMessage(account, row.getExternalMessageId());
        transactionTemplate.executeWithoutResult(status ->
                mailMapper.deleteLabelMapByType(empId, mailId, "TRASH"));
        return new MailMutationResponse(mailId, "RESTORED");
    }

    @Override
    public MailSyncResponse syncMails(int maxResults) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        return syncAccount(account, maxResults);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MailSummaryResponse> getMailsForWidget() {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        List<MailSummaryResponse> mails = mailMapper.selectMails(
                empId,
                "INBOX",
                null,
                account.getEmailAddr(),
                0,
                WIDGET_MAIL_LIMIT);
        fillLabels(empId, mails);
        return mails;
    }
    
    @Override
    public MailSyncResponse syncAccount(MailAccountVO account, int maxResults) {
        Long empId = account.getEmpId();
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        int limit = normalizeSyncLimit(maxResults);
        boolean incremental = account.getGoogleHistoryId() != null
                && !account.getGoogleHistoryId().isBlank();
        GmailSyncResult result = googleGmailClient.syncMessages(account, account.getGoogleHistoryId(), limit);
        if (result == null) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
        return transactionTemplate.execute(status -> persistSyncResult(account, result, incremental));
    }

    private MailSyncResponse persistSyncResult(MailAccountVO account, GmailSyncResult result, boolean incremental) {
        Long empId = account.getEmpId();
        ensureSystemLabels(empId);
        List<GmailSyncedMessage> messages = result.getMessages() == null
                ? List.of()
                : result.getMessages();

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int newInboxCount = 0;
        String latestInboxSubject = null;
        for (GmailSyncedMessage message : messages) {
            if (message == null || message.getExternalMessageId() == null || message.getExternalMessageId().isBlank()) {
                skipped++;
                continue;
            }

            Long mailId = mailMapper.selectMailIdByExternalMessageId(empId, message.getExternalMessageId());
            boolean newMessage = mailId == null;
            if (mailId == null) {
                mailId = mailMapper.selectNextMailMessageId();
                mailMapper.insertMailMessage(toMailMessageRow(empId, mailId, message));
                inserted++;
            } else {
                MailMessageRow row = toMailMessageRow(empId, mailId, message);
                mailMapper.updateMailMessage(row);
                mailMapper.deleteParticipantsByMail(empId, mailId);
                mailMapper.deleteLabelMapsByMail(empId, mailId);
                updated++;
            }

            syncParticipants(empId, mailId, message);
            syncLabels(empId, mailId, message.getLabels());
            if (newMessage) {
                syncAttachments(empId, mailId, message.getAttachments());
            }
            if (newMessage && message.getLabels() != null
                    && message.getLabels().contains("INBOX")) {
                newInboxCount++;
                if (latestInboxSubject == null) {
                    latestInboxSubject = message.getSubject();
                }
            }
        }

        String latestHistoryId = latestHistoryId(result, account);
        if (latestHistoryId != null && !latestHistoryId.isBlank()) {
            mailMapper.updateMailAccountSyncState(empId, latestHistoryId);
        }

        if (incremental && newInboxCount > 0) {
            eventPublisher.publishEvent(new MailReceivedEvent(empId, newInboxCount, latestInboxSubject));
        }

        return new MailSyncResponse(
                messages.size(),
                inserted,
                updated,
                skipped,
                latestHistoryId,
                LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public MailAttachmentDownload downloadAttachment(Long mailId, Long attachmentId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        if (mailId == null || attachmentId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        MailAttachmentResponse meta = mailMapper.selectAttachmentMeta(empId, mailId, attachmentId);
        if (meta == null) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
        Resource resource = fileService.download(attachmentId);
        return new MailAttachmentDownload(resource, meta.getOriginalFileName(), meta.getContentType());
    }

    @Override
    public MailBulkResponse bulkAction(MailBulkRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (request == null || request.getMailIds() == null || request.getMailIds().isEmpty()
                || request.getAction() == null || request.getAction().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String action = request.getAction().trim().toLowerCase(Locale.ROOT);
        if (!BULK_ACTIONS.contains(action)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        PermissionCode permission = "trash".equals(action)
                ? PermissionCode.MAIL_DELETE
                : PermissionCode.MAIL_READ;
        assertPermission(permission, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);

        boolean important = Boolean.TRUE.equals(request.getImportant());
        int processed = 0;
        int failed = 0;
        for (Long mailId : request.getMailIds()) {
            try {
                applyBulkAction(empId, account, mailId, action, important);
                processed++;
            } catch (Exception e) {
                log.warn("Bulk mail action failed. action={}, mailId={}", action, mailId, e);
                failed++;
            }
        }
        return new MailBulkResponse(processed, failed);
    }

    private void applyBulkAction(Long empId, MailAccountVO account, Long mailId, String action, boolean important) {
        MailMessageRow row = loadMailRow(empId, mailId);
        applyBulkGmailAction(account, row, action, important);
        transactionTemplate.executeWithoutResult(status ->
                applyBulkLocalAction(empId, mailId, action, important));
    }

    private void applyBulkGmailAction(MailAccountVO account, MailMessageRow row, String action, boolean important) {
        switch (action) {
            case "read" -> {
                googleGmailClient.markRead(account, row.getExternalMessageId());
            }
            case "unread" -> {
                googleGmailClient.markUnread(account, row.getExternalMessageId());
            }
            case "trash" -> {
                googleGmailClient.trashMessage(account, row.getExternalMessageId());
            }
            case "important" -> {
                googleGmailClient.updateImportant(account, row.getExternalMessageId(), important);
            }
            default -> throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void applyBulkLocalAction(Long empId, Long mailId, String action, boolean important) {
        switch (action) {
            case "read" -> mailMapper.deleteLabelMapByType(empId, mailId, "UNREAD");
            case "unread" -> {
                ensureSystemLabels(empId);
                addLabel(empId, mailId, "UNREAD");
            }
            case "trash" -> {
                ensureSystemLabels(empId);
                addLabel(empId, mailId, "TRASH");
            }
            case "important" -> {
                ensureSystemLabels(empId);
                if (important) {
                    addLabel(empId, mailId, "IMPORTANT");
                } else {
                    mailMapper.deleteLabelMapByType(empId, mailId, "IMPORTANT");
                }
            }
            default -> throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Override
    @Transactional
    public Long saveDraft(MailDraftRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_SEND, empId);
        MailAccountVO account = loadAccount(empId);
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<String> to = normalizeEmails(request.getTo());
        List<String> cc = normalizeEmails(request.getCc());
        List<String> bcc = normalizeEmails(request.getBcc());
        String subject = request.getSubject() == null ? "" : request.getSubject().trim();
        String content = request.getContent() == null ? "" : request.getContent();
        LocalDateTime now = LocalDateTime.now();

        Long mailId = request.getMailId();
        if (mailId == null) {
            mailId = mailMapper.selectNextMailMessageId();
            mailMapper.insertMailMessage(buildDraftRow(empId, mailId, account, subject, content, to, now, null, null, null));
        } else {
            MailMessageRow existing = mailMapper.selectMailRow(empId, mailId);
            if (existing == null || !"Y".equals(existing.getDraftYn())) {
                throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
            }
            mailMapper.updateMailMessage(buildDraftRow(empId, mailId, account, subject, content, to, now,
                    existing.getExternalMessageId(), existing.getThreadId(), existing.getMessageIdHeader()));
            mailMapper.deleteParticipantsByMail(empId, mailId);
        }

        for (String email : to) {
            insertParticipant(empId, mailId, email, "TO");
        }
        for (String email : cc) {
            insertParticipant(empId, mailId, email, "CC");
        }
        for (String email : bcc) {
            insertParticipant(empId, mailId, email, "BCC");
        }
        return mailId;
    }

    private MailMessageRow buildDraftRow(Long empId, Long mailId, MailAccountVO account, String subject,
            String content, List<String> to, LocalDateTime now,
            String externalMessageId, String threadId, String messageIdHeader) {
        MailMessageRow row = new MailMessageRow();
        row.setMailId(mailId);
        row.setEmpId(empId);
        // 드래프트는 Gmail 외부 ID가 없으므로 NOT NULL 충돌 방지를 위해 고유 플레이스홀더를 채운다.
        row.setExternalMessageId(externalMessageId != null ? externalMessageId : "DRAFT_" + mailId);
        row.setThreadId(threadId != null ? threadId : "DRAFT_" + mailId);
        row.setMessageIdHeader(messageIdHeader);
        row.setSubject(subject);
        row.setContent(content);
        row.setSnippet(buildSnippet(content));
        row.setFromEmail(account.getEmailAddr());
        row.setToSummary(buildToSummary(to));
        row.setSentAt(now);
        row.setInternalDate(now);
        row.setDraftYn("Y");
        row.setBodySyncYn("Y");
        row.setDelYn("N");
        return row;
    }

    @Override
    @Transactional(readOnly = true)
    public MailDetailResponse getDraft(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailMessageRow row = mailMapper.selectMailRow(empId, mailId);
        if (row == null || !"Y".equals(row.getDraftYn())) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
        MailDetailResponse detail = mailMapper.selectMailDetail(empId, mailId);
        if (detail == null) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
        detail.setParticipants(mailMapper.selectParticipants(empId, mailId));
        detail.setAttachments(List.of());
        detail.setLabels(List.of());
        applyRenderPolicy(detail);
        return detail;
    }

    @Override
    @Transactional
    public void deleteDraft(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_SEND, empId);
        MailMessageRow row = mailMapper.selectMailRow(empId, mailId);
        if (row == null || !"Y".equals(row.getDraftYn())) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
        deleteDraftRow(empId, mailId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MailLabelResponse> getUserLabels() {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        return mailMapper.selectUserLabels(empId);
    }

    @Override
    @Transactional
    public MailLabelResponse createUserLabel(MailLabelRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_MANAGE, empId);
        String name = normalizeLabelName(request);
        if (mailMapper.countUserLabelName(empId, name) > 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Long labelId = mailMapper.selectNextMailLabelId();
        mailMapper.insertUserLabel(labelId, empId, name);
        return buildLabelResponse(labelId, name);
    }

    @Override
    @Transactional
    public MailLabelResponse renameUserLabel(Long labelId, MailLabelRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_MANAGE, empId);
        requireUserLabel(empId, labelId);
        String name = normalizeLabelName(request);
        mailMapper.updateUserLabelName(empId, labelId, name);
        return buildLabelResponse(labelId, name);
    }

    @Override
    @Transactional
    public void deleteUserLabel(Long labelId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_MANAGE, empId);
        requireUserLabel(empId, labelId);
        mailMapper.deleteLabelMapsByLabel(empId, labelId);
        mailMapper.softDeleteUserLabel(empId, labelId);
    }

    @Override
    @Transactional
    public void applyLabel(Long mailId, Long labelId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        requireUserLabel(empId, labelId);
        requireMail(empId, mailId);
        if (mailMapper.existsLabelMap(empId, mailId, labelId) == 0) {
            mailMapper.insertLabelMap(mailMapper.selectNextMailLabelMapId(), empId, mailId, labelId);
        }
    }

    @Override
    @Transactional
    public void removeLabel(Long mailId, Long labelId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        requireUserLabel(empId, labelId);
        mailMapper.deleteLabelMapByLabelId(empId, mailId, labelId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MailSummaryResponse> getMailsByLabel(Long labelId, Pageable pageable) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        requireUserLabel(empId, labelId);
        long total = mailMapper.countMailsByLabel(empId, labelId);
        List<MailSummaryResponse> mails =
                mailMapper.selectMailsByLabel(empId, labelId, pageable.getOffset(), pageable.getPageSize());
        fillLabels(empId, mails);
        return new PageImpl<>(mails, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MailLabelResponse> getMailLabels(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        return mailMapper.selectUserLabelsForMail(empId, mailId);
    }

    private MailLabelResponse buildLabelResponse(Long labelId, String name) {
        MailLabelResponse response = new MailLabelResponse();
        response.setLabelId(labelId);
        response.setName(name);
        return response;
    }

    private String normalizeLabelName(MailLabelRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return request.getName().trim();
    }

    private void requireUserLabel(Long empId, Long labelId) {
        if (labelId == null || mailMapper.selectUserLabel(empId, labelId) == null) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
    }

    private void requireMail(Long empId, Long mailId) {
        if (mailId == null || mailMapper.selectMailRow(empId, mailId) == null) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MailUnreadCountResponse getUnreadCount() {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        return new MailUnreadCountResponse(mailMapper.countUnreadInbox(empId));
    }

    private void assertPermission(PermissionCode permissionCode, Long empId) {
        authorizationService.assertCurrentUserPermission(
                permissionCode,
                ResourceContext.builder()
                        .resourceType(ResourceType.MAIL)
                        .ownerEmpId(empId)
                        .build());
    }

    private MailAccountVO loadAccount(Long empId) {
        MailAccountVO account = mailMapper.selectActiveMailAccount(empId);
        if (account == null) {
            throw new CustomException(ErrorCode.MAIL_ACCOUNT_NOT_FOUND);
        }
        if (isReconnectRequired(account)) {
            throw new CustomException(ErrorCode.MAIL_TOKEN_INVALID);
        }
        return account;
    }

    private String normalizeTokenStatus(MailAccountVO account) {
        String tokenStatus = account.getTokenStatusCd();
        if (tokenStatus == null || tokenStatus.isBlank()) {
            return TOKEN_STATUS_ACTIVE;
        }
        return tokenStatus.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isReconnectRequired(MailAccountVO account) {
        String tokenStatus = normalizeTokenStatus(account);
        return TOKEN_STATUS_INVALID.equals(tokenStatus) || TOKEN_STATUS_REVOKED.equals(tokenStatus);
    }

    private void applyRenderPolicy(MailDetailResponse detail) {
        detail.setContentRenderMode(CONTENT_RENDER_MODE_SANDBOX_IFRAME);
    }

    private MailMessageRow loadMailRow(Long empId, Long mailId) {
        if (mailId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        MailMessageRow row = mailMapper.selectMailRow(empId, mailId);
        if (row == null) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
        if (row.getExternalMessageId() == null || row.getExternalMessageId().isBlank()) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
        return row;
    }

    private void requireScope(MailAccountVO account, String scope) {
        if (!hasScope(account, scope)) {
            throw new CustomException(ErrorCode.MAIL_SCOPE_REQUIRED);
        }
    }

    private void requireAnyScope(MailAccountVO account, String... scopes) {
        for (String scope : scopes) {
            if (hasScope(account, scope)) {
                return;
            }
        }
        throw new CustomException(ErrorCode.MAIL_SCOPE_REQUIRED);
    }

    private boolean hasScope(MailAccountVO account, String requiredScope) {
        String scopeCn = account.getScopeCn();
        if (scopeCn == null || requiredScope == null) {
            return false;
        }
        for (String scope : scopeCn.split("\\s+|,")) {
            if (requiredScope.equals(scope.trim())) {
                return true;
            }
        }
        return false;
    }

    private void ensureSystemLabels(Long empId) {
        for (String label : SYSTEM_LABELS) {
            mergeSystemLabelSafely(empId, label);
        }
    }

    /**
     * 시스템 라벨을 보장한다.
     * MAIL_LABEL_ID를 MAX(ID)+1로 채번하므로 동시 동기화 시 PK 충돌(ORA-00001)이 발생할 수 있다.
     * 충돌이 나면 이미 해당 라벨이 생성됐는지 확인하고, 아니면 ID를 다시 채번해 재시도한다.
     */
    private void mergeSystemLabelSafely(Long empId, String label) {
        final int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                mailMapper.mergeSystemLabel(
                        mailMapper.selectNextMailLabelId(),
                        empId,
                        label,
                        label,
                        label);
                return;
            } catch (DuplicateKeyException e) {
                // 다른 트랜잭션이 같은 ID/라벨을 막 생성한 경우: 이미 존재하면 정상 종료
                if (mailMapper.selectLabelIdByType(empId, label) != null) {
                    return;
                }
                // 존재하지 않으면 ID 채번 충돌이므로 다시 채번해 재시도
                if (attempt == maxAttempts) {
                    throw e;
                }
            }
        }
    }

    private void addLabel(Long empId, Long mailId, String labelType) {
        Long labelId = mailMapper.selectLabelIdByType(empId, labelType);
        if (labelId == null) {
            ensureSystemLabels(empId);
            labelId = mailMapper.selectLabelIdByType(empId, labelType);
        }
        if (labelId == null) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
        if (mailMapper.existsLabelMap(empId, mailId, labelId) == 0) {
            mailMapper.insertLabelMap(mailMapper.selectNextMailLabelMapId(), empId, mailId, labelId);
        }
    }

    private void fillLabels(Long empId, List<MailSummaryResponse> mails) {
        for (MailSummaryResponse mail : mails) {
            mail.setMailboxTypes(mailMapper.selectLabelTypes(empId, mail.getMailId()));
        }
    }

    private String normalizeType(String type) {
        String normalized = type == null || type.isBlank()
                ? "inbox"
                : type.trim().toLowerCase(Locale.ROOT);
        if (!MAILBOX_TYPES.contains(normalized)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private List<String> normalizeEmails(List<String> emails) {
        if (emails == null) {
            return List.of();
        }
        Set<String> deduped = new LinkedHashSet<>();
        for (String email : emails) {
            if (email != null && !email.isBlank()) {
                deduped.add(email.trim());
            }
        }
        return new ArrayList<>(deduped);
    }

    private List<String> emailsByType(List<MailParticipantResponse> participants, String type) {
        if (participants == null || participants.isEmpty()) {
            return List.of();
        }
        List<String> emails = participants.stream()
                .filter(participant -> type.equals(participant.getType()))
                .map(MailParticipantResponse::getEmail)
                .toList();
        return normalizeEmails(emails);
    }

    private void deleteDraftRow(Long empId, Long mailId) {
        mailMapper.deleteParticipantsByMail(empId, mailId);
        mailMapper.deleteLabelMapsByMail(empId, mailId);
        mailMapper.markMessagesDeleted(empId, List.of(mailId));
    }

    private void insertParticipant(Long empId, Long mailId, String email, String type) {
        MailParticipantRow participant = new MailParticipantRow();
        participant.setParticipantId(mailMapper.selectNextMailParticipantId());
        participant.setEmpId(empId);
        participant.setMailId(mailId);
        participant.setEmail(email);
        participant.setType(type);
        mailMapper.insertParticipant(participant);
    }

    private void syncParticipants(Long empId, Long mailId, GmailSyncedMessage message) {
        insertParticipantIfPresent(empId, mailId, message.getFromEmail(), "FROM");
        for (String email : normalizeEmails(message.getTo())) {
            insertParticipant(empId, mailId, email, "TO");
        }
        for (String email : normalizeEmails(message.getCc())) {
            insertParticipant(empId, mailId, email, "CC");
        }
        for (String email : normalizeEmails(message.getBcc())) {
            insertParticipant(empId, mailId, email, "BCC");
        }
        insertParticipantIfPresent(empId, mailId, message.getReplyToEmail(), "REPLY_TO");
    }

    private void insertParticipantIfPresent(Long empId, Long mailId, String email, String type) {
        if (email != null && !email.isBlank()) {
            insertParticipant(empId, mailId, email.trim(), type);
        }
    }

    private void syncLabels(Long empId, Long mailId, List<String> labels) {
        for (String label : labels == null ? List.<String>of() : labels) {
            if (label != null && SYSTEM_LABELS.contains(label.trim())) {
                addLabel(empId, mailId, label.trim());
            }
        }
    }

    private void syncAttachments(Long empId, Long mailId, List<GmailSyncedAttachment> attachments) {
        for (GmailSyncedAttachment attachment : attachments == null ? List.<GmailSyncedAttachment>of() : attachments) {
            if (attachment == null || attachment.getContent() == null || attachment.getContent().length == 0) {
                continue;
            }
            FileUploadRequestDto uploadRequest = new FileUploadRequestDto();
            uploadRequest.setFile(new InMemoryMailMultipartFile(
                    attachment.getOriginalFileName(),
                    attachment.getContentType(),
                    attachment.getContent()));
            uploadRequest.setFileCn("메일 수신 첨부파일");
            Long attachmentId = fileService.upload(uploadRequest, MAIL_ATTACHMENT_BIZ_CD);
            mailMapper.insertAttachment(attachmentId, empId, mailId);
        }
    }

    private MailMessageRow toMailMessageRow(Long empId, Long mailId, GmailSyncedMessage message) {
        MailMessageRow row = new MailMessageRow();
        row.setMailId(mailId);
        row.setEmpId(empId);
        row.setExternalMessageId(message.getExternalMessageId());
        row.setThreadId(message.getThreadId());
        row.setMessageIdHeader(message.getMessageIdHeader());
        row.setSubject(message.getSubject());
        row.setContent(message.getContent());
        row.setSnippet(message.getSnippet());
        row.setFromEmail(message.getFromEmail());
        row.setToSummary(buildToSummary(normalizeEmails(message.getTo())));
        row.setSentAt(message.getSentAt());
        row.setInternalDate(message.getInternalDate());
        row.setDraftYn("N");
        row.setBodySyncYn(message.getContent() == null || message.getContent().isBlank() ? "N" : "Y");
        row.setDelYn("N");
        return row;
    }

    private String latestHistoryId(GmailSyncResult result, MailAccountVO account) {
        if (result.getLatestHistoryId() != null && !result.getLatestHistoryId().isBlank()) {
            return result.getLatestHistoryId();
        }
        return account.getGoogleHistoryId();
    }

    private int normalizeSyncLimit(int maxResults) {
        if (maxResults <= 0) {
            return 50;
        }
        return Math.min(maxResults, 200);
    }

    private String buildToSummary(List<String> to) {
        if (to == null || to.isEmpty()) {
            return null;
        }
        if (to.size() == 1) {
            return to.getFirst();
        }
        return to.getFirst() + " 외 " + (to.size() - 1) + "명";
    }

    private String buildSnippet(String content) {
        if (content == null) {
            return null;
        }
        String plain = content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        return plain.length() > 180 ? plain.substring(0, 180) : plain;
    }
}

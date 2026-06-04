package com.mycrewsoft.domain.mail.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.request.MailSendRequest;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;
import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSendCommand;
import com.mycrewsoft.domain.mail.gmail.GmailSendResult;
import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;
import com.mycrewsoft.domain.mail.vo.MailMessageRow;
import com.mycrewsoft.domain.mail.vo.MailParticipantRow;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final String SCOPE_GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly";
    private static final String SCOPE_GMAIL_SEND = "https://www.googleapis.com/auth/gmail.send";
    private static final String SCOPE_GMAIL_MODIFY = "https://www.googleapis.com/auth/gmail.modify";
    private static final String MAIL_ATTACHMENT_BIZ_CD = "05";
    private static final Set<String> MAILBOX_TYPES = Set.of("inbox", "sent", "all", "self", "tome");
    private static final List<String> SYSTEM_LABELS = List.of("INBOX", "SENT", "TRASH", "UNREAD", "IMPORTANT");

    private final AuthorizationService authorizationService;
    private final MailMapper mailMapper;
    private final GoogleGmailClient googleGmailClient;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public List<MailSummaryResponse> getMails(String type, String keyword, Pageable pageable) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        String normalizedType = normalizeType(type);
        List<MailSummaryResponse> mails = mailMapper.selectMails(
                empId,
                normalizedType,
                normalizeKeyword(keyword),
                account.getEmailAddr(),
                pageable.getOffset(),
                pageable.getPageSize());
        fillLabels(empId, mails);
        return mails;
    }

    @Override
    @Transactional
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

        GmailSendCommand command = new GmailSendCommand();
        command.setFromEmail(account.getEmailAddr());
        command.setTo(to);
        command.setCc(cc);
        command.setBcc(bcc);
        command.setSubject(request.getSubject().trim());
        command.setContent(request.getContent());
        command.setAttachments(uploadFiles);

        GmailSendResult result = googleGmailClient.sendMessage(account, command);
        if (result == null || result.getExternalMessageId() == null) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }

        ensureSystemLabels(empId);

        Long mailId = mailMapper.selectNextMailMessageId();
        LocalDateTime sentAt = result.getSentAt() == null ? LocalDateTime.now() : result.getSentAt();
        MailMessageRow row = new MailMessageRow();
        row.setMailId(mailId);
        row.setEmpId(empId);
        row.setExternalMessageId(result.getExternalMessageId());
        row.setThreadId(result.getThreadId());
        row.setSubject(request.getSubject().trim());
        row.setContent(request.getContent());
        row.setSnippet(buildSnippet(request.getContent()));
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
    @Transactional
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
            mailMapper.updateMailBody(empId, mailId, content.getContent(), content.getSnippet());
        }

        MailDetailResponse detail = mailMapper.selectMailDetail(empId, mailId);
        if (detail == null) {
            throw new CustomException(ErrorCode.MAIL_NOT_FOUND);
        }
        detail.setParticipants(mailMapper.selectParticipants(empId, mailId));
        detail.setAttachments(mailMapper.selectAttachments(empId, mailId));
        detail.setLabels(mailMapper.selectLabelTypes(empId, mailId));
        return detail;
    }

    @Override
    @Transactional
    public MailMutationResponse moveToTrash(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_DELETE, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.trashMessage(account, row.getExternalMessageId());
        ensureSystemLabels(empId);
        addLabel(empId, mailId, "TRASH");
        return new MailMutationResponse(mailId, "TRASHED");
    }

    @Override
    @Transactional
    public MailMutationResponse markRead(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.markRead(account, row.getExternalMessageId());
        mailMapper.deleteLabelMapByType(empId, mailId, "UNREAD");
        return new MailMutationResponse(mailId, "READ");
    }

    @Override
    @Transactional
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
        ensureSystemLabels(empId);
        if (Boolean.TRUE.equals(request.getImportant())) {
            addLabel(empId, mailId, "IMPORTANT");
        } else {
            mailMapper.deleteLabelMapByType(empId, mailId, "IMPORTANT");
        }
        return new MailMutationResponse(mailId, "IMPORTANT_UPDATED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MailSummaryResponse> getTrash(Pageable pageable) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireAnyScope(account, SCOPE_GMAIL_READONLY, SCOPE_GMAIL_MODIFY);

        List<MailSummaryResponse> mails = mailMapper.selectTrash(empId, pageable.getOffset(), pageable.getPageSize());
        fillLabels(empId, mails);
        return mails;
    }

    @Override
    @Transactional
    public MailTrashClearResponse clearTrash() {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_DELETE, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);

        List<MailMessageRow> trashRows = mailMapper.selectTrashRows(empId);
        if (trashRows.isEmpty()) {
            return new MailTrashClearResponse(0);
        }

        for (MailMessageRow row : trashRows) {
            googleGmailClient.deleteMessage(account, row.getExternalMessageId());
        }
        mailMapper.markMessagesDeleted(empId, trashRows.stream().map(MailMessageRow::getMailId).toList());
        return new MailTrashClearResponse(trashRows.size());
    }

    @Override
    @Transactional
    public MailMutationResponse restore(Long mailId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        assertPermission(PermissionCode.MAIL_READ, empId);
        MailAccountVO account = loadAccount(empId);
        requireScope(account, SCOPE_GMAIL_MODIFY);
        MailMessageRow row = loadMailRow(empId, mailId);

        googleGmailClient.untrashMessage(account, row.getExternalMessageId());
        mailMapper.deleteLabelMapByType(empId, mailId, "TRASH");
        return new MailMutationResponse(mailId, "RESTORED");
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
        return account;
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
            mailMapper.mergeSystemLabel(
                    mailMapper.selectNextMailLabelId(),
                    empId,
                    label,
                    label,
                    label);
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

    private void insertParticipant(Long empId, Long mailId, String email, String type) {
        MailParticipantRow participant = new MailParticipantRow();
        participant.setParticipantId(mailMapper.selectNextMailParticipantId());
        participant.setEmpId(empId);
        participant.setMailId(mailId);
        participant.setEmail(email);
        participant.setType(type);
        mailMapper.insertParticipant(participant);
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

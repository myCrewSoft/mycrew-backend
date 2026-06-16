package com.mycrewsoft.domain.mail.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.request.MailBulkRequest;
import com.mycrewsoft.domain.mail.dto.request.MailDraftRequest;
import com.mycrewsoft.domain.mail.dto.request.MailSendRequest;
import com.mycrewsoft.domain.mail.dto.response.MailBulkResponse;
import com.mycrewsoft.domain.mail.dto.response.MailAttachmentDownload;
import com.mycrewsoft.domain.mail.dto.response.MailAccountStatusResponse;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSyncResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

public interface MailService {

    MailAccountStatusResponse getAccountStatus();

    Page<MailSummaryResponse> getMails(String type, String keyword, Pageable pageable);

    MailSendResponse sendMail(MailSendRequest request, List<MultipartFile> attachments);

    MailDetailResponse getMail(Long mailId);

    MailAttachmentDownload downloadAttachment(Long mailId, Long attachmentId);

    MailMutationResponse moveToTrash(Long mailId);

    MailMutationResponse markRead(Long mailId);

    MailMutationResponse markUnread(Long mailId);

    MailMutationResponse updateImportant(Long mailId, MailImportantUpdateRequest request);

    Page<MailSummaryResponse> getTrash(Pageable pageable);

    MailTrashClearResponse clearTrash();

    MailMutationResponse restore(Long mailId);

    MailSyncResponse syncMails(int maxResults);

    MailSyncResponse syncAccount(MailAccountVO account, int maxResults);

    MailBulkResponse bulkAction(MailBulkRequest request);

    Long saveDraft(MailDraftRequest request);

    MailDetailResponse getDraft(Long mailId);

    void deleteDraft(Long mailId);
    
    // 위젯용
    List<MailSummaryResponse> getMailsForWidget();
}

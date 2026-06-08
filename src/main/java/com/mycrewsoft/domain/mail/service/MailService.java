package com.mycrewsoft.domain.mail.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.request.MailSendRequest;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSyncResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;

public interface MailService {

    Page<MailSummaryResponse> getMails(String type, String keyword, Pageable pageable);

    MailSendResponse sendMail(MailSendRequest request, List<MultipartFile> attachments);

    MailDetailResponse getMail(Long mailId);

    MailMutationResponse moveToTrash(Long mailId);

    MailMutationResponse markRead(Long mailId);

    MailMutationResponse updateImportant(Long mailId, MailImportantUpdateRequest request);

    Page<MailSummaryResponse> getTrash(Pageable pageable);

    MailTrashClearResponse clearTrash();

    MailMutationResponse restore(Long mailId);

    MailSyncResponse syncMails(int maxResults);
}

package com.mycrewsoft.domain.mail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSyncResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;
import com.mycrewsoft.domain.mail.service.MailService;

class MailControllerTest {

    @Test
    void getMailsReturnsServiceResponse() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailSummaryResponse mail = new MailSummaryResponse();
        mail.setMailId(1L);
        mail.setSubject("subject");
        mail.setSentAt(LocalDateTime.now());
        Page<MailSummaryResponse> page = new PageImpl<>(List.of(mail), PageRequest.of(0, 20), 21);
        when(service.getMails("inbox", null, PageRequest.of(0, 20))).thenReturn(page);

        ResponseEntity<ApiResponse<List<MailSummaryResponse>>> response =
                controller.getMails("inbox", null, 0, 20);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactly(mail);
        assertThat(response.getBody().getPagination()).isNotNull();
        assertThat(response.getBody().getPagination().getPage()).isZero();
        assertThat(response.getBody().getPagination().getSize()).isEqualTo(20);
        assertThat(response.getBody().getPagination().getTotalElements()).isEqualTo(21);
        verify(service).getMails("inbox", null, PageRequest.of(0, 20));
    }

    @Test
    void getTrashReturnsPaginationResponse() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailSummaryResponse mail = new MailSummaryResponse();
        mail.setMailId(2L);
        Page<MailSummaryResponse> page = new PageImpl<>(List.of(mail), PageRequest.of(1, 10), 25);
        when(service.getTrash(PageRequest.of(1, 10))).thenReturn(page);

        ResponseEntity<ApiResponse<List<MailSummaryResponse>>> response = controller.getTrash(1, 10);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(mail);
        assertThat(response.getBody().getPagination()).isNotNull();
        assertThat(response.getBody().getPagination().getPage()).isEqualTo(1);
        assertThat(response.getBody().getPagination().getSize()).isEqualTo(10);
        assertThat(response.getBody().getPagination().getTotalElements()).isEqualTo(25);
        verify(service).getTrash(PageRequest.of(1, 10));
    }

    @Test
    void markReadDelegatesToService() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailMutationResponse mutation = new MailMutationResponse(10L, "READ");
        when(service.markRead(10L)).thenReturn(mutation);

        ResponseEntity<ApiResponse<MailMutationResponse>> response = controller.markRead(10L);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(mutation);
        verify(service).markRead(10L);
    }

    @Test
    void updateImportantDelegatesToService() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailImportantUpdateRequest request = new MailImportantUpdateRequest();
        request.setImportant(true);
        MailMutationResponse mutation = new MailMutationResponse(10L, "IMPORTANT_UPDATED");
        when(service.updateImportant(10L, request)).thenReturn(mutation);

        ResponseEntity<ApiResponse<MailMutationResponse>> response = controller.updateImportant(10L, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(mutation);
        verify(service).updateImportant(10L, request);
    }

    @Test
    void clearTrashReturnsDeletedCount() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailTrashClearResponse result = new MailTrashClearResponse(3);
        when(service.clearTrash()).thenReturn(result);

        ResponseEntity<ApiResponse<MailTrashClearResponse>> response = controller.clearTrash();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(result);
        verify(service).clearTrash();
    }

    @Test
    void syncDelegatesToService() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailSyncResponse result = new MailSyncResponse(2, 1, 1, 0, "history-2", LocalDateTime.now());
        when(service.syncMails(30)).thenReturn(result);

        ResponseEntity<ApiResponse<MailSyncResponse>> response = controller.syncMails(30);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(result);
        verify(service).syncMails(30);
    }

    @Test
    void sendDraftDelegatesToService() {
        MailService service = Mockito.mock(MailService.class);
        MailController controller = new MailController(service);
        MailSendResponse result = new MailSendResponse(100L, "gmail-sent", "thread-sent", LocalDateTime.now());
        when(service.sendDraft(10L)).thenReturn(result);

        ResponseEntity<ApiResponse<MailSendResponse>> response = controller.sendDraft(10L);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(result);
        verify(service).sendDraft(10L);
    }
}

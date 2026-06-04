package com.mycrewsoft.domain.mail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
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
        when(service.getMails("inbox", null, PageRequest.of(0, 20))).thenReturn(List.of(mail));

        ResponseEntity<ApiResponse<List<MailSummaryResponse>>> response =
                controller.getMails("inbox", null, 0, 20);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactly(mail);
        verify(service).getMails("inbox", null, PageRequest.of(0, 20));
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
}

package com.mycrewsoft.domain.mail.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mail.dto.request.MailImportantUpdateRequest;
import com.mycrewsoft.domain.mail.dto.request.MailSendRequest;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;
import com.mycrewsoft.domain.mail.service.MailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mails")
@RequiredArgsConstructor
@Tag(name = "메일", description = "Google Workspace 메일 API")
public class MailController {

    private final MailService mailService;

    @GetMapping
    @Operation(summary = "메일 목록 조회", description = "메일함 유형, 키워드, 페이지 조건으로 현재 사원의 메일 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MailSummaryResponse>>> getMails(
            @Parameter(description = "메일함 유형: inbox, sent, all, self, tome")
            @RequestParam(defaultValue = "inbox") String type,
            @Parameter(description = "제목, 발신자, 수신자, 스니펫 검색어")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Page<MailSummaryResponse> mailPage = mailService.getMails(type, keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(mailPage.getContent(), mailPage));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "메일 작성 및 발송", description = "수신자, 제목, 본문, 첨부파일을 포함한 메일을 Gmail로 발송합니다.")
    public ResponseEntity<ApiResponse<MailSendResponse>> sendMail(
            @Valid @RequestPart("request") MailSendRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        return ResponseEntity.ok(ApiResponse.success(mailService.sendMail(request, attachments)));
    }

    @GetMapping("/{mailId}")
    @Operation(summary = "메일 상세 조회", description = "현재 사원에게 속한 메일 상세 정보와 참여자, 첨부파일, 라벨을 조회합니다.")
    public ResponseEntity<ApiResponse<MailDetailResponse>> getMail(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.getMail(mailId)));
    }

    @DeleteMapping("/{mailId}")
    @Operation(summary = "메일 휴지통 이동", description = "메일을 Gmail 휴지통으로 이동하고 로컬 라벨 상태를 동기화합니다.")
    public ResponseEntity<ApiResponse<MailMutationResponse>> moveToTrash(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.moveToTrash(mailId)));
    }

    @PatchMapping("/{mailId}/read")
    @Operation(summary = "메일 읽음 처리", description = "Gmail UNREAD 라벨을 제거하고 로컬 읽음 상태를 동기화합니다.")
    public ResponseEntity<ApiResponse<MailMutationResponse>> markRead(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.markRead(mailId)));
    }

    @PatchMapping("/{mailId}/important")
    @Operation(summary = "중요 메일 설정 변경", description = "중요 메일 라벨을 설정하거나 해제합니다.")
    public ResponseEntity<ApiResponse<MailMutationResponse>> updateImportant(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId,
            @Valid @org.springframework.web.bind.annotation.RequestBody MailImportantUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mailService.updateImportant(mailId, request)));
    }

    @GetMapping("/trash")
    @Operation(summary = "휴지통 목록 조회", description = "현재 사원의 휴지통 메일 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MailSummaryResponse>>> getTrash(
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Page<MailSummaryResponse> trashPage = mailService.getTrash(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(trashPage.getContent(), trashPage));
    }

    @DeleteMapping("/trash")
    @Operation(summary = "휴지통 비우기", description = "현재 사원의 휴지통 메일을 Gmail에서 영구 삭제하고 로컬 삭제 상태를 반영합니다.")
    public ResponseEntity<ApiResponse<MailTrashClearResponse>> clearTrash() {
        return ResponseEntity.ok(ApiResponse.success(mailService.clearTrash()));
    }

    @PostMapping("/trash/{mailId}/restore")
    @Operation(summary = "휴지통 메일 복원", description = "Gmail 휴지통에서 메일을 복원하고 로컬 TRASH 라벨을 제거합니다.")
    public ResponseEntity<ApiResponse<MailMutationResponse>> restore(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.restore(mailId)));
    }
}

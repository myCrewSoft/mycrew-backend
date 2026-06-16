package com.mycrewsoft.domain.mail.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import com.mycrewsoft.domain.mail.dto.request.MailBulkRequest;
import com.mycrewsoft.domain.mail.dto.request.MailDraftRequest;
import com.mycrewsoft.domain.mail.dto.request.MailLabelRequest;
import com.mycrewsoft.domain.mail.dto.request.MailSendRequest;
import com.mycrewsoft.domain.mail.dto.response.MailAccountStatusResponse;
import com.mycrewsoft.domain.mail.dto.response.MailAttachmentDownload;
import com.mycrewsoft.domain.mail.dto.response.MailBulkResponse;
import com.mycrewsoft.domain.mail.dto.response.MailLabelResponse;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailMutationResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSendResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSyncResponse;
import com.mycrewsoft.domain.mail.dto.response.MailTrashClearResponse;
import com.mycrewsoft.domain.mail.dto.response.MailUnreadCountResponse;
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

    @GetMapping("/account/status")
    @Operation(summary = "메일 계정 연동 상태 조회", description = "메일 계정 존재 여부와 토큰 상태를 조회합니다. 토큰이 만료되었거나 무효인 계정도 재연동 대상으로 응답합니다.")
    public ResponseEntity<ApiResponse<MailAccountStatusResponse>> getAccountStatus() {
        return ResponseEntity.ok(ApiResponse.success(mailService.getAccountStatus()));
    }

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

    @PostMapping("/sync")
    @Operation(summary = "Gmail 메일 동기화", description = "Gmail에서 신규/변경 메일을 가져와 로컬 메일 DB에 반영합니다.")
    public ResponseEntity<ApiResponse<MailSyncResponse>> syncMails(
            @Parameter(description = "최대 동기화 메시지 수")
            @RequestParam(defaultValue = "50") int maxResults) {
        return ResponseEntity.ok(ApiResponse.success(mailService.syncMails(maxResults)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "받은편지함 안읽은 메일 수", description = "헤더 메일 아이콘 배지용으로 안읽은 받은 메일 수를 조회합니다.")
    public ResponseEntity<ApiResponse<MailUnreadCountResponse>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(mailService.getUnreadCount()));
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

    @GetMapping("/{mailId}/attachments/{attachmentId}")
    @Operation(summary = "메일 첨부파일 다운로드", description = "현재 사원에게 속한 메일의 첨부파일을 다운로드합니다.")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId,
            @Parameter(description = "첨부파일 ID") @PathVariable Long attachmentId) {
        MailAttachmentDownload download = mailService.downloadAttachment(mailId, attachmentId);
        String fileName = download.getFileName() == null ? "attachment" : download.getFileName();
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(download.getResource());
    }

    @PatchMapping("/{mailId}/unread")
    @Operation(summary = "메일 읽지 않음 처리", description = "Gmail UNREAD 라벨을 추가하고 로컬 읽지 않음 상태를 동기화합니다.")
    public ResponseEntity<ApiResponse<MailMutationResponse>> markUnread(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.markUnread(mailId)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "메일 일괄 처리", description = "선택한 여러 메일을 한 번에 읽음/읽지않음/휴지통/중요 처리합니다. action: read, unread, trash, important.")
    public ResponseEntity<ApiResponse<MailBulkResponse>> bulkAction(
            @Valid @org.springframework.web.bind.annotation.RequestBody MailBulkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mailService.bulkAction(request)));
    }

    @PostMapping("/drafts")
    @Operation(summary = "임시보관 메일 저장", description = "작성 중인 메일을 임시보관(드래프트)으로 저장하거나 기존 드래프트를 수정합니다.")
    public ResponseEntity<ApiResponse<Long>> saveDraft(
            @Valid @org.springframework.web.bind.annotation.RequestBody MailDraftRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mailService.saveDraft(request)));
    }

    @GetMapping("/drafts/{mailId}")
    @Operation(summary = "임시보관 메일 조회", description = "편집을 위해 임시보관 메일의 수신자/제목/본문을 조회합니다.")
    public ResponseEntity<ApiResponse<MailDetailResponse>> getDraft(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.getDraft(mailId)));
    }

    @PostMapping("/drafts/{mailId}/send")
    @Operation(summary = "임시보관 메일 전송", description = "저장된 임시보관 메일의 수신자, 제목, 본문으로 Gmail 메일을 전송하고 임시보관 메일을 삭제 처리합니다.")
    public ResponseEntity<ApiResponse<MailSendResponse>> sendDraft(
            @Parameter(description = "임시보관 메일 ID") @PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.sendDraft(mailId)));
    }

    @DeleteMapping("/drafts/{mailId}")
    @Operation(summary = "임시보관 메일 삭제", description = "임시보관 메일을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @Parameter(description = "내부 메일 ID") @PathVariable Long mailId) {
        mailService.deleteDraft(mailId);
        return ResponseEntity.ok(ApiResponse.success("임시보관 메일을 삭제했습니다.", null));
    }

    @GetMapping("/labels")
    @Operation(summary = "사용자 라벨 목록", description = "현재 사원의 사용자 정의 라벨 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MailLabelResponse>>> getUserLabels() {
        return ResponseEntity.ok(ApiResponse.success(mailService.getUserLabels()));
    }

    @PostMapping("/labels")
    @Operation(summary = "사용자 라벨 생성", description = "사용자 정의 라벨을 생성합니다.")
    public ResponseEntity<ApiResponse<MailLabelResponse>> createUserLabel(
            @Valid @org.springframework.web.bind.annotation.RequestBody MailLabelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mailService.createUserLabel(request)));
    }

    @PatchMapping("/labels/{labelId}")
    @Operation(summary = "사용자 라벨 이름 변경", description = "사용자 정의 라벨 이름을 변경합니다.")
    public ResponseEntity<ApiResponse<MailLabelResponse>> renameUserLabel(
            @PathVariable Long labelId,
            @Valid @org.springframework.web.bind.annotation.RequestBody MailLabelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mailService.renameUserLabel(labelId, request)));
    }

    @DeleteMapping("/labels/{labelId}")
    @Operation(summary = "사용자 라벨 삭제", description = "사용자 정의 라벨을 삭제하고 모든 메일에서 라벨을 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteUserLabel(@PathVariable Long labelId) {
        mailService.deleteUserLabel(labelId);
        return ResponseEntity.ok(ApiResponse.success("라벨을 삭제했습니다.", null));
    }

    @GetMapping("/labels/{labelId}/mails")
    @Operation(summary = "라벨별 메일 목록", description = "특정 사용자 라벨이 적용된 메일 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponse<List<MailSummaryResponse>>> getMailsByLabel(
            @PathVariable Long labelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MailSummaryResponse> mailPage = mailService.getMailsByLabel(labelId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(mailPage.getContent(), mailPage));
    }

    @GetMapping("/{mailId}/labels")
    @Operation(summary = "메일의 사용자 라벨 조회", description = "특정 메일에 적용된 사용자 라벨 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MailLabelResponse>>> getMailLabels(@PathVariable Long mailId) {
        return ResponseEntity.ok(ApiResponse.success(mailService.getMailLabels(mailId)));
    }

    @PostMapping("/{mailId}/labels/{labelId}")
    @Operation(summary = "메일에 라벨 적용", description = "특정 메일에 사용자 라벨을 적용합니다.")
    public ResponseEntity<ApiResponse<Void>> applyLabel(@PathVariable Long mailId, @PathVariable Long labelId) {
        mailService.applyLabel(mailId, labelId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{mailId}/labels/{labelId}")
    @Operation(summary = "메일에서 라벨 제거", description = "특정 메일에서 사용자 라벨을 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> removeLabel(@PathVariable Long mailId, @PathVariable Long labelId) {
        mailService.removeLabel(mailId, labelId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

package com.mycrewsoft.domain.messenger.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.messenger.dto.request.AddParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.RemoveParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.enums.ParticipantStatus;
import com.mycrewsoft.domain.messenger.service.MsngrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Messenger", description = "메신저 관련 API")
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class MsngrController {

    private final MsngrService msngrService;

    @Operation(summary = "채팅방 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getChtrmList() {
        return ResponseEntity.ok(ApiResponse.success(msngrService.getChtrmList()));
    }

    @Operation(summary = "채팅방 단건 조회")
    @GetMapping("/{chtrmId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChtrm(
            @Parameter(description = "채팅방 ID", example = "1")
            @PathVariable Long chtrmId) {
        return ResponseEntity.ok(ApiResponse.success(msngrService.getChtrm(chtrmId)));
    }

    @Operation(summary = "채팅 메시지 목록 조회")
    @GetMapping("/{chtrmId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMsgList(
            @Parameter(description = "채팅방 ID", example = "1")
            @PathVariable Long chtrmId) {
        return ResponseEntity.ok(ApiResponse.success(msngrService.getMsgList(chtrmId)));
    }

    @Operation(summary = "채팅방 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createChtrm(
            @Validated @RequestBody CreateChatRoomRequest request) {
        Long chtrmId = msngrService.createChtrm(request);
        return ResponseEntity.ok(ApiResponse.success("채팅방이 생성되었습니다.", chtrmId));
    }

    @Operation(summary = "채팅방 수정")
    @PutMapping("/{chtrmId}")
    public ResponseEntity<ApiResponse<Void>> updateChtrm(
        @PathVariable Long chtrmId,    
        @Validated @RequestBody UpdateChatRoomRequest request
    ) {
        msngrService.updateChtrm(chtrmId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "채팅방 삭제")
    @DeleteMapping("/{chtrmId}")
    public ResponseEntity<ApiResponse<Void>> deleteChtrm(
        @PathVariable Long chtrmId  
    ) {
        msngrService.deleteChtrm(chtrmId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "참여자 추가")
    @PostMapping("/{chtrmId}/participants")
    public ResponseEntity<ApiResponse<Void>> addChtrmPtcpt(
        @PathVariable Long chtrmId,
        @Validated @RequestBody AddParticipantsRequest request
    ) {
        msngrService.addChtrmPtcpt(chtrmId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "참여자 삭제")
    @DeleteMapping("/{chtrmId}/participants")
    public ResponseEntity<ApiResponse<Void>> removeChtrmPtcpt(
        @PathVariable Long chtrmId,
        @Validated @RequestBody RemoveParticipantsRequest request
    ) {
        msngrService.removeChtrmPtcpt(chtrmId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "참여자 상태 변경 (자리비움, 다른 업무 중 등)")
    @PatchMapping("/status/{ptcptSttusCd}")
    public ResponseEntity<ApiResponse<Void>> updatePtcptSttus(
            @Parameter(description = "상태 코드 (STS1: 로그인, STS2: 자리비움, STS3: 다른 업무 중, STS4: 로그아웃)", example = "STS2")
            @PathVariable String ptcptSttusCd) {
        msngrService.updatePtcptSttus(ParticipantStatus.fromCode(ptcptSttusCd));
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "마지막 읽은 메시지 갱신 (읽음 처리)")
    @PatchMapping("/{chtrmId}/read/{msgId}")
    public ResponseEntity<ApiResponse<Void>> updateLastCfmtnMsgId(
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chtrmId,
            @Parameter(description = "마지막으로 읽은 메시지 ID", example = "101") @PathVariable Long msgId) {
        msngrService.updateLastCfmtnMsgId(chtrmId, msgId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

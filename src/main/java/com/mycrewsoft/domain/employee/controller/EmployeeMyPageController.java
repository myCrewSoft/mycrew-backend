package com.mycrewsoft.domain.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.ChangeEmailRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeJobDutyRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangePasswordRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeProfileImageRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeSignatureRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeMyPageResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.service.MyPageService;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "직원 마이페이지 조회 및 개인정보 변경 API")
public class EmployeeMyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "프로필 정보 조회", description = "로그인한 사용자의 헤더 표시용 간략 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> profile() {
        EmployeeProfileDTO response = myPageService.getEmployeeProfile();
        return ResponseEntity.ok(ApiResponse.success("프로필 정보 조회 성공", response));
    }

    @Operation(summary = "마이페이지 정보 조회", description = "로그인한 사용자의 마이페이지 상세 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeMyPageResponseDTO>> myPage() {
        EmployeeMyPageResponseDTO response = myPageService.getEmployeeMyPage();
        return ResponseEntity.ok(ApiResponse.success("마이페이지 정보 조회 성공", response));
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        myPageService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공"));
    }

    @Operation(
            summary = "이메일 변경 Google OAuth URL 발급",
            description = "입력한 Google 이메일 주소와 실제 OAuth 인증 계정이 일치할 때 메일 계정 이메일을 변경할 수 있도록 인증 URL을 발급합니다."
    )
    @PatchMapping("/email")
    public ResponseEntity<ApiResponse<GoogleOAuthAuthorizeResponse>> changeEmail(
            @Valid @RequestBody ChangeEmailRequestDTO request) {
        GoogleOAuthAuthorizeResponse response = myPageService.changeEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Google 이메일 재연동 URL 발급 성공", response));
    }

    @Operation(summary = "전자서명 변경", description = "로그인한 사용자의 전자서명 파일 ID를 변경합니다.")
    @PatchMapping("/signature")
    public ResponseEntity<ApiResponse<String>> changeSignature(
            @Valid @RequestBody ChangeSignatureRequestDTO request) {
        myPageService.changeSignature(request);
        return ResponseEntity.ok(ApiResponse.success("전자서명 변경 성공"));
    }

    @Operation(summary = "프로필 이미지 변경", description = "로그인한 사용자의 프로필 이미지 파일 ID를 변경합니다.")
    @PatchMapping("/profile-image")
    public ResponseEntity<ApiResponse<String>> changeProfileImage(
            @Valid @RequestBody ChangeProfileImageRequestDTO request) {
        myPageService.changeProfileImage(request);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 변경 성공"));
    }

    @Operation(summary = "직무 변경", description = "로그인한 사용자의 직무 내용을 변경합니다.")
    @PatchMapping("/job-duty")
    public ResponseEntity<ApiResponse<String>> changeJobDuty(
            @Valid @RequestBody ChangeJobDutyRequestDTO request) {
        myPageService.changeJobDuty(request);
        return ResponseEntity.ok(ApiResponse.success("직무 변경 성공"));
    }
}

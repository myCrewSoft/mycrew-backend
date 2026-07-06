package com.mycrewsoft.domain.employee.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.dto.request.ChangeEmailRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeJobDutyRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangePasswordRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeProfileInfoRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeMyPageResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.event.PasswordChangedEvent;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.file.constant.FileConstants;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileServiceImpl;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;
import com.mycrewsoft.domain.mail.service.GoogleOAuthService;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {
	private final EmployeeMapper employeeMapper;
	private final PasswordEncoder passwordEncoder;
	private final GoogleOAuthService googleOAuthService;
	private final FileServiceImpl fileService;
	private final ApplicationEventPublisher eventPublisher;
	
	@Override
	@Transactional(readOnly = true)
	public EmployeeProfileDTO getEmployeeProfile() {
		Long empId = SecurityUtil.getCurrentEmpId();
		return employeeMapper.selectEmployeeProfileByEmpId(empId);
	}

	@Override
	@Transactional(readOnly = true)
	public EmployeeMyPageResponseDTO getEmployeeMyPage() {
		Long empId = SecurityUtil.getCurrentEmpId();
		return employeeMapper.selectEmployeeMyPageByEmpId(empId);
	}

	@Override
	@Transactional
	public void changePassword(ChangePasswordRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		String storedPasswordHash = employeeMapper.selectPasswordByEmpId(empId);
		
		if (!passwordEncoder.matches(request.getCurrentPassword(), storedPasswordHash)) {
			throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
		}
		
		String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
		int updatedCount = employeeMapper.updatePasswordByEmpId(empId, newPasswordHash);
		
		eventPublisher.publishEvent(
			new PasswordChangedEvent(empId)
		);
	}

	@Override
	@Transactional
	public GoogleOAuthAuthorizeResponse changeEmail(ChangeEmailRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		return googleOAuthService.createAuthorizationUrl(empId, "mypage-email", request.getEmailAddr());
	}

	@Override
	@Transactional
	public Long changeSignature(FileUploadRequestDto file) {
		Long empId = SecurityUtil.getCurrentEmpId();

		// 기존 fileService를 경유하여 전자서명 이미지를 업로드한다.
		Long stampFileId = fileService.upload(file, FileConstants.SIGNATURE);
		employeeMapper.updateSignatureByEmpId(empId, stampFileId);

		return stampFileId;
	}

	@Override
	@Transactional
	public Long changeProfileImage(FileUploadRequestDto file) {
		Long empId = SecurityUtil.getCurrentEmpId();

		// 기존 fileService를 경유하여 프로필 이미지를 업로드한다.
		Long prflImgFileId = fileService.upload(file, FileConstants.PROFILE);
		employeeMapper.updateProfileImageByEmpId(empId, prflImgFileId);

		return prflImgFileId;
	}

	@Override
	@Transactional
	public void changeJobDuty(ChangeJobDutyRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		int updatedCount = employeeMapper.updateJobDutyByEmpId(empId, request.getJobDutyCn());
	}

	@Override
	@Transactional
	public void changeProfileInfo(ChangeProfileInfoRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		employeeMapper.updateProfileInfoByEmpId(
				empId,
				request.getEmpNm(),
				emptyToNull(request.getMblTelno()),
				emptyToNull(request.getZip()),
				emptyToNull(request.getAddr()));
	}

	private String emptyToNull(String value) {
		return (value == null || value.isBlank()) ? null : value.trim();
	}
}

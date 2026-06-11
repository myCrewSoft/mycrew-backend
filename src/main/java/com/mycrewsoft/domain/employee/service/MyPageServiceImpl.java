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
import com.mycrewsoft.domain.employee.dto.request.ChangeProfileImageRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeSignatureRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeMyPageResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.event.PasswordChangedEvent;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
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
	public void changeSignature(ChangeSignatureRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		int updatedCount = employeeMapper.updateSignatureByEmpId(empId, request.getMbrStampFileId());
	}

	@Override
	@Transactional
	public void changeProfileImage(FileUploadRequestDto file, ChangeProfileImageRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		
		Long driveAtchFileId = fileService.upload(file, "03");
		int updatedCount = employeeMapper.updateProfileImageByEmpId(empId, request.getPrflImgFileId());
	}

	@Override
	@Transactional
	public void changeJobDuty(ChangeJobDutyRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		int updatedCount = employeeMapper.updateJobDutyByEmpId(empId, request.getJobDutyCn());
	}
}

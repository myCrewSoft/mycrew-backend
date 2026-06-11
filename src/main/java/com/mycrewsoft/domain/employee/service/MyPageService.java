package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.ChangeEmailRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeJobDutyRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangePasswordRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeMyPageResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;

public interface MyPageService {
	public EmployeeProfileDTO getEmployeeProfile();
	public EmployeeMyPageResponseDTO getEmployeeMyPage();
	public void changePassword(ChangePasswordRequestDTO request);
	public GoogleOAuthAuthorizeResponse changeEmail(ChangeEmailRequestDTO request);
	/** 전자서명 이미지를 업로드하고 변경한 뒤 새 파일 ID를 반환한다. */
	public Long changeSignature(FileUploadRequestDto file);
	/** 프로필 이미지를 업로드하고 변경한 뒤 새 파일 ID를 반환한다. */
	public Long changeProfileImage(FileUploadRequestDto file);
	public void changeJobDuty(ChangeJobDutyRequestDTO request);
}

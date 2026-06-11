package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.ChangeEmailRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeJobDutyRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangePasswordRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeProfileImageRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.ChangeSignatureRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeMyPageResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;

public interface MyPageService {
	public EmployeeProfileDTO getEmployeeProfile();
	public EmployeeMyPageResponseDTO getEmployeeMyPage();
	public void changePassword(ChangePasswordRequestDTO request);
	public GoogleOAuthAuthorizeResponse changeEmail(ChangeEmailRequestDTO request);
	public void changeSignature(ChangeSignatureRequestDTO request);
	public void changeProfileImage(FileUploadRequestDto file, ChangeProfileImageRequestDTO request);
	public void changeJobDuty(ChangeJobDutyRequestDTO request);
}

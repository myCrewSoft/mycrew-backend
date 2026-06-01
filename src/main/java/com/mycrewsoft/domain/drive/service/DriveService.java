package com.mycrewsoft.domain.drive.service;

import java.util.List;

import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;

public interface DriveService {
	//폴더 생성
	DriveResponseDto createFolder(DriveFolderCreateRequestDto reqDto);
	
	//파일 업로드
	DriveResponseDto uploadFile(FileUploadRequestDto fileReqDto, Long prntDriveItemId);
	
	/**
	 * 개인 드라이브 목록 조회
	 * @return
	 */
	List<DriveResponseDto> getMyDriveList();
}

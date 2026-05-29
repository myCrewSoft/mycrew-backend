package com.mycrewsoft.domain.drive.service;

import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;

public interface DriveService {
	//폴더 생성
	DriveResponseDto createFolder(DriveFolderCreateRequestDto reqDto);
}

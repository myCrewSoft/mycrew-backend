package com.mycrewsoft.domain.drive.service;

import org.springframework.data.domain.Page;

import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveRenameRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.dto.DriveSearchRequestDto;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;

public interface ProjectDriveService {
	
	/**
	 * 프로젝트 드라이브 조회
	 * @param projId
	 * @param reqDto
	 * @return
	 */
	Page<DriveResponseDto> getProjectDriveList(Long projId, DriveSearchRequestDto reqDto);
	
	/**
	 * 폴더 생성
	 * @param projId
	 * @param reqDto
	 */
	void createFolder(Long projId, DriveFolderCreateRequestDto reqDto);
	
	
	/**
	 * 파일 업로드
	 * @param projId
	 * @param reqDto
	 * @param prntDriveItemId
	 */
	void uploadFile(Long projId, FileUploadRequestDto reqDto, Long prntDriveItemId);
	
	/**
	 * 프로젝트 드라이브 폴더명 수정
	 * @param driveItemId
	 * @param reqDto
	 */
	void renameItem(Long driveItemId, DriveRenameRequestDto reqDto);
	
	/**
	 * 프로젝트 드라이브 즐겨찾기 등록/해제
	 * @param driveItemId
	 * @param bookmarkYn
	 */
	void toggleBookmark(Long driveItemId);
}

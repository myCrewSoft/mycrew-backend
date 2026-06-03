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
	List<DriveResponseDto> getMyDriveList(Long prntDriveItemId);

	/**
	 * 드라이브 폴더명 수정
	 * @param driveItemId
	 * @param itemNm
	 * @return
	 */
	DriveResponseDto renameItem(Long driveItemId, String itemNm);
	
	/**
	 * 즐겨찾기 등록 (즐겨찾기여부 상태 변경)
	 * @param driveItemId
	 * @return
	 */
	DriveResponseDto registerBookmark(Long driveItemId);
	
	/**
	 * 즐겨찾기 해제 (즐겨찾기여부 상태 변경)
	 * @param driveItemId
	 * @return
	 */
	DriveResponseDto deleteBookmark(Long driveItemId);
	
	/**
	 * 드라이브 아이템 단건 삭제 (soft delete)
	 * @param driveItemId
	 * @return
	 */
	void deleteItem(Long driveItemId);
}

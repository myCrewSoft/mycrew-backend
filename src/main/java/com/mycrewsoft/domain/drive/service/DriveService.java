package com.mycrewsoft.domain.drive.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.dto.DriveSearchRequestDto;
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
	Page<DriveResponseDto> getMyDriveList(DriveSearchRequestDto reqDto);

	/**
	 * 드라이브 폴더명 수정
	 * @param driveItemId
	 * @param itemNm
	 * @return
	 */
	DriveResponseDto renameItem(Long driveItemId, String itemNm);
	
	/**
	 * 즐겨찾기 등록/해제 (즐겨찾기여부 상태 변경)
	 * @param driveItemId
	 * @return
	 */
	DriveResponseDto toggleBookmark(Long driveItemId);
	
	/**
	 * 드라이브 아이템 단건 삭제 (soft delete)
	 * @param driveItemId
	 * @return
	 */
	void deleteItem(Long driveItemId);
	
	/**
	 * 개인 드라이브 휴지통 목록 조회
	 * @param empId
	 * @return
	 */
	List<DriveResponseDto> getTrashList();
	
	/**
	 * 휴지통 복원
	 * @param driveItemId
	 */
	void restoreItem(Long driveItemId);
	
	/**
	 * 영구삭제
	 * @param driveItemId
	 */
	void hardDeleteItem(Long driveItemId);
	
	/**
	 * 파일 다운로드
	 * @param driveItemId
	 * @return
	 */
	ResponseEntity<Resource> downloadFile(Long driveItemId);
}

package com.mycrewsoft.domain.file.service;

import java.util.List;

import com.mycrewsoft.domain.file.dto.FileDtlResponseDto;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;

public interface FileService {
	//파일 업로드
	void upload(FileUploadRequestDto reqDto, String bizCd);
	
	//파일 단건 조회
	FileDtlResponseDto getFile(Long atchFileId);
	
	//파일 목록 조회
	List<FileDtlResponseDto> getFileList(Long atchFileId);
	
	//파일 삭제
	void deleteFile(Long atchFileDtlId, Long loginUserId);
}

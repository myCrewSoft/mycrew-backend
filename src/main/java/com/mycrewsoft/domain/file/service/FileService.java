package com.mycrewsoft.domain.file.service;

import java.util.List;

import org.springframework.core.io.Resource;

import com.mycrewsoft.domain.file.dto.FileDtlResponseDto;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;

public interface FileService {
	//파일 업로드
	Long upload(FileUploadRequestDto reqDto, String bizCd);
	
	//파일 삭제 (논리 삭제)
	void deleteFile(Long atchFileDtlId);
	
	//파일 다운로드
	Resource download(Long atchFileDtlId);
	
	//파일 복원
	void restoreFile(Long atchFileDtlId);
	
	//파일 영구 삭제
	void hardDeleteFile(Long atchFileDtlId);
}

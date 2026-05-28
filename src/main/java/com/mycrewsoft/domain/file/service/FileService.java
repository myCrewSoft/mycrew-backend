package com.mycrewsoft.domain.file.service;

import java.util.List;

import org.springframework.core.io.Resource;

import com.mycrewsoft.domain.file.dto.FileDtlResponseDto;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;

public interface FileService {
	//파일 업로드
	void upload(FileUploadRequestDto reqDto, String bizCd);
	
	//파일 삭제
	void deleteFile(Long atchFileDtlId);
	
	//파일 다운로드
	Resource download(Long atchFileDtlId);
}

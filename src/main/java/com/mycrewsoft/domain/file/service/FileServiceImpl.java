package com.mycrewsoft.domain.file.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.file.dto.FileDtlResponseDto;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.mapper.FileMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
	
	private final FileMapper mapper;
	
	/**
	 * 파일 업로드 처리
	 */
	@Override
	@Transactional
	public void upload(FileUploadRequestDto reqDto) {
		MultipartFile file = reqDto.getFile();
		
		//파일 검증
		String originalFileName = file.getOriginalFilename();
		String extension = FileUtil.getExtension(originalFileName);
		
		String atchFileTyCd;
		if(Set.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
			atchFileTyCd = "01";
		}else {
			atchFileTyCd = "02";
		}
		
		//저장용 파일명 생성
				
		//실제 파일 저장
				
		//확장자, 파일사이즈, 저장경로 세팅
		
		mapper.insertClsf();
		mapper.insertDtl(reqDto);
	}

	@Override
	public FileDtlResponseDto getFile(Long atchFileId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FileDtlResponseDto> getFileList(Long atchFileId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteFile(Long atchFileDtlId, Long loginUserId) {
		// TODO Auto-generated method stub
		
	}

}

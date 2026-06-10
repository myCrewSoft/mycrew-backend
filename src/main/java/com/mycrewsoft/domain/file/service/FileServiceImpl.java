package com.mycrewsoft.domain.file.service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.mapper.FileMapper;
import com.mycrewsoft.domain.file.vo.FileClsfVo;
import com.mycrewsoft.domain.file.vo.FileDtlVo;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
	private final DtoMapper dtoMapper;
	private final FileMapper mapper;
	
	@Value("${file.upload-path}")
	private String uploadPath;
	
	/**
	 * 파일 업로드 처리
	 */
	@Override
	@Transactional
	public Long upload(FileUploadRequestDto reqDto, String bizCd) {
		MultipartFile file = reqDto.getFile();
		
		String originalFileName = file.getOriginalFilename();
		String extension = FileUtil.getExtension(originalFileName);
		
		//파일 검증 및 첨부파일타입코드 설정
		String atchFileTyCd;
		
		if(Set.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
			FileUtil.validateImageFile(file);
			atchFileTyCd = "01";
		}else {
			FileUtil.validateDocumentFile(file);
			atchFileTyCd = "02";
		}
		
		//저장용 파일명 생성
		String saveFileNm = FileUtil.generateStoredFileName(originalFileName);
		//실제 파일 저장
		try {
			Path savePath = Paths.get(uploadPath, saveFileNm);
			file.transferTo(savePath);
		}catch(Exception e){
			throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
		}
		
		FileClsfVo fileClsfVo = new FileClsfVo();
		fileClsfVo.setAtchFileBizCd(bizCd);
		mapper.insertClsf(fileClsfVo);
		
		//사용자 정보
		Long frstRgstrId = SecurityUtil.getCurrentEmpId();
		
		//reqDto -> VO 변환 및 VO 생성
		FileDtlVo fileDtlVo = new FileDtlVo();
		fileDtlVo.setFileCn(reqDto.getFileCn());
		fileDtlVo.setAtchFileId(fileClsfVo.getAtchFileId());
		fileDtlVo.setAtchFileTyCd(atchFileTyCd);
		fileDtlVo.setFileExtsn(extension);
		fileDtlVo.setFileSz(file.getSize());
		fileDtlVo.setFrstRgstrId(frstRgstrId);
		fileDtlVo.setOrgnlFileNm(originalFileName);
		fileDtlVo.setSaveFileNm(saveFileNm);
		fileDtlVo.setSavePathNm(uploadPath);
		
		mapper.insertDtl(fileDtlVo);
		
		return fileDtlVo.getAtchFileDtlId();
		//호출하는 쪽에서 첨부파일상세ID를 사용할 수 있도록 반환 (첨부파일ID 논리FK로 사용하는 경우)
	}

	/**
	 * 파일 삭제 처리
	 */
	@Override
	public void deleteFile(Long atchFileDtlId) {
		Long currentEmpId = SecurityUtil.getCurrentEmpId();
		
		int result = mapper.deleteDtl(atchFileDtlId, currentEmpId);
		
		if(result == 0) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
	}

	/**
	 * 파일 다운로드
	 */
	@Override
	@Transactional
	public Resource download(Long atchFileDtlId) {
		//파일이 존재하는지 정보 조회
		FileDtlVo fileDtlVo = mapper.selectDtlById(atchFileDtlId);
		if(fileDtlVo == null || fileDtlVo.getDelYn() == "Y") {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
		
		//실제 파일 경로 확인
		Path savePathNm = Paths.get(fileDtlVo.getSavePathNm(), fileDtlVo.getSaveFileNm());
		if (!Files.exists(savePathNm)) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
		
		//검증에 다 통과를 한다면 실제 파일 반환
		try {
			return new UrlResource(savePathNm.toUri());
		}catch(MalformedURLException e){
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
	}

	/**
	 * 파일 복원
	 */
	@Override
	public void restoreFile(Long atchFileDtlId) {
		int result = mapper.restoreFile(atchFileDtlId);
		if(result == 0) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
	}

	/**
	 * 파일 영구 삭제
	 * @return 
	 */
	@Override
	@Transactional
	public void hardDeleteFile(Long atchFileDtlId) {
		//파일 정보 조회 (실제 저장 경로 가져오기)
		FileDtlVo fileDtlVo	= mapper.selectDtlById(atchFileDtlId);
		if(fileDtlVo == null) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
		
		//디스크에서 실제 파일 삭제
		Path filePath = Paths.get(fileDtlVo.getSavePathNm(), fileDtlVo.getSaveFileNm());
		try {
			Files.deleteIfExists(filePath);
		}catch(Exception e) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
		
		//무결성 제약 조건으로 상세 삭제 후 -> 분류 삭제
		int result1 = mapper.hardDeleteFileDtl(atchFileDtlId);
		if(result1 == 0) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}else {
			int result2 = mapper.hardDeleteFileClsf(fileDtlVo.getAtchFileId());
			if(result2 == 0) {
				throw new CustomException(ErrorCode.FILE_NOT_FOUND);
			}
		}
	}

	/**
	 * 이미지 서빙
	 */
	@Override
	public Resource serveImage(Long atchFileDtlId) {
		//파일 존재 여부 확인
		FileDtlVo dtlVo = mapper.selectDtlById(atchFileDtlId);
		if(dtlVo == null || "Y".equals(dtlVo.getDelYn())) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
		
		//이미지 파일인지 확인 (첨부파일타입코드 "01" = IMAGE)
		if(!"01".equals(dtlVo.getAtchFileTyCd())) {
			throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
		}
		
		//저장 경로 확인
		Path savePath = Paths.get(dtlVo.getSavePathNm(), dtlVo.getSaveFileNm());
		if(!Files.exists(savePath)) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
		try {
			return new UrlResource(savePath.toUri());
		}catch(MalformedURLException e) {
			throw new CustomException(ErrorCode.FILE_NOT_FOUND);
		}
	}
	
	
	
}

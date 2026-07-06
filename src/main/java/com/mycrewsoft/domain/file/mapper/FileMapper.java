package com.mycrewsoft.domain.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.vo.FileClsfVo;
import com.mycrewsoft.domain.file.vo.FileDtlVo;

@Mapper
public interface FileMapper {
	//분류 등록
	int insertClsf(FileClsfVo fileClsfVo);
	
	//상세 등록
	int insertDtl(FileDtlVo fileDtlVo);
	
	//삭제 (delYn 'Y'로 업데이트)
	int deleteDtl(@Param("atchFileDtlId") Long atchFileDtlId, @Param("dltrsId") Long dltrsId);
	
	//파일 정보 조회
	FileDtlVo selectDtlById(@Param("atchFileDtlId") Long atchFileDtlId);
	
	//파일 복원 (파일 삭제 상태 변경)
	int restoreFile(Long atchFileDtlId);
	
	//파일 영구 삭제
	int hardDeleteFileClsf(Long atchFileId);
	int hardDeleteFileDtl(Long atchFileDtlId);
}

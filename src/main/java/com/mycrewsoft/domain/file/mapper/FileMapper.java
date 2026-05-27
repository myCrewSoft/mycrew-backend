package com.mycrewsoft.domain.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.vo.FileDtlVo;

@Mapper
public interface FileMapper {
	//분류 등록
	int insertClsf();
	
	//상세 등록
	int insertDtl(FileUploadRequestDto reqDto);
	
	//상세 단건 조회
	FileDtlVo selectDtlById(Long atchFileDtlId);
	
	//분류 기준 상세 목록 조회
	List<FileDtlVo> selectDtlListByClsfId(Long atchFileId);
	
	//삭제 (delYn 'Y'로 업데이트)
	int deleteDtl(Long atchFileDtlId);
}

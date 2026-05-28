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
}

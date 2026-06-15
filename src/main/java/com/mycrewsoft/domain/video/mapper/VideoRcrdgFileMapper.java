package com.mycrewsoft.domain.video.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.file.vo.FileClsfVo;
import com.mycrewsoft.domain.file.vo.FileDtlVo;

@Mapper
public interface VideoRcrdgFileMapper {

    // 녹취록 첨부파일 분류 등록 (TB_CMMN_ATCH_FILE_CLSF)
    void insertClsf(FileClsfVo vo);

    // 녹취록 첨부파일 상세 등록 (TB_CMMN_ATCH_FILE_DTL)
    void insertDtl(FileDtlVo vo);

    // 녹취록 첨부파일 상세 단건 조회 (재생/다운로드용)
    FileDtlVo selectDtlByAtchFileId(Long atchFileId);
}
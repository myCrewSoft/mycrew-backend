package com.mycrewsoft.domain.mtng.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.mtng.vo.mom.MtngMomHistVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;

@Mapper
public interface MtngMomMapper {

    // 회의록 등록 (AI 초안 상태로 최초 생성)
    void createMtngMom(MtngMomVO vo);

    // 회의ID로 회의록 단건 조회 (수정 이력 포함)
    MtngMomVO selectMomByMtngId(@Param("mtngId") Long mtngId);

    // 회의록 내용 및 상태 수정
    void updateMtngMom(MtngMomVO vo);

    // 결재 요청 시 DRFT_DOC_SN 연결 + 상태 변경
    void updateMtngMomDrftDocSn(@Param("momId") Long momId, @Param("drftDocSn") Long drftDocSn, @Param("momSttusCd") String momSttusCd);

    // 회의록 수정 이력 단건 등록
    void createMtngMomHist(MtngMomHistVO vo);
}
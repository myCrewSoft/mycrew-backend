package com.mycrewsoft.domain.mtng.mapper;

import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngListVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptVO;
import com.mycrewsoft.domain.mtng.vo.MtngVO;
import com.mycrewsoft.domain.video.vo.VideoConfVO;

import io.lettuce.core.dynamic.annotation.Param;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MtngMapper {

    // TB_MTNG에 INSERT, 시퀀스로 생성된 MTNG_ID를 mtngVO.mtngId에 채워줌
    void createMtng(MtngVO mtngVO);

    // TB_MTNG_PTCPT에 참여자 목록 INSERT (1건씩 반복 INSERT)
    void createMtngPtcptList(List<MtngPtcptVO> ptcptVOList);

    // TB_MTNG.CONF_RM_RSRV_ID UPDATE (회의실 연결)
    void updateMtngConfRmRsrvId(Long mtngId, Long confRmRsrvId);

    // 회의 목록 조회
    List<MtngListVO> selectMtngList(
        @Param("empId") Long empId,
        @Param("keyword") String keyword,
        @Param("beginDt") LocalDateTime beginDt,
        @Param("endDt") LocalDateTime endDt
    );

    // 상세 조회
    MtngDetailVO selectMtngDetail(@Param("mtngId") Long mtngId);

    // 참여자 상세 목록
    List<MtngPtcptDetailVO> selectMtngPtcptDetailList(@Param("mtngId") Long mtngId);

    // 수정: TB_MTNG 기본정보 UPDATE
    void updateMtng(MtngVO mtngVO);

    // 수정: 참여자 전체 삭제 후 재등록 (DELETE -> INSERT ALL 패턴)
    void deleteMtngPtcptByMtngId(@Param("mtngId") Long mtngId);

    // 삭제: 논리삭제
    void deleteMtng(@Param("mtngId") Long mtngId);

    // 삭제 검증용: 회의 시작시각 조회
    LocalDateTime selectMtngBeginDt(@Param("mtngId") Long mtngId);

    // 화상회의 정보 조회 (수정 시 타입변경 분기에 사용)
    VideoConfVO selectVideoConfByMtngId(@Param("mtngId") Long mtngId);

    // 화상회의 row 삭제 (ONLINE/HYBRID -> OFFLINE 변경 시)
    void deleteVideoConfByMtngId(@Param("mtngId") Long mtngId);

    // 화상회의 ID로 회의 조회
    MtngDetailVO selectMtngDetailByVconfId(@Param("vconfId") Long vconfId);
}
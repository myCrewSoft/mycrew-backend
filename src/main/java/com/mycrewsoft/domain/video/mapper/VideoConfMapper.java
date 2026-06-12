package com.mycrewsoft.domain.video.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.video.vo.VideoConfVO;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;
import com.mycrewsoft.domain.video.vo.VideoConfListVO;
import com.mycrewsoft.domain.video.vo.VideoMomAprvlVO;
import com.mycrewsoft.domain.video.vo.VideoMomHistVO;
import com.mycrewsoft.domain.video.vo.VideoMomVO;
import com.mycrewsoft.domain.video.vo.VideoPtcptVO;
import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;

@Mapper
public interface VideoConfMapper {

    // 화상회의 단건 등록
    void insertConf(VideoConfVO vo);

    // 화상회의 방 이름 확정 (채번된 vconfId 기반으로 roomNm 업데이트)
    void updateRoomNm(@Param("vconfId") Long vconfId, @Param("roomNm") String roomNm);

    // 화상회의 참여자 등록
    void insertPtcpt(VideoPtcptVO vo);

    // 화상회의 단건 조회 (참여자 목록 포함)
    VideoConfListVO selectConfById(@Param("vconfId") Long vconfId);

    // 로그인한 사원이 참여 중인 화상회의 목록 조회
    List<VideoConfListVO> selectConfList(@Param("empId") Long empId);

    // 참여자 입장 시각 업데이트 (LiveKit 실제 입장 시점)
    void updatePtcptJoinDt(@Param("vconfPtcptId") Long vconfPtcptId);

    // 화상회의 상태 코드 수정 (01: 대기 / 02: 진행 중 / 03: 종료)
    void updateConfSttus(@Param("vconfId") Long vconfId, @Param("confSttusCd") String confSttusCd);

    // 화상회의 ID로 회의록 단건 조회 (결재 목록, 수정 이력 포함)
    VideoMomVO selectMomByVconfId(@Param("vconfId") Long vconfId);

    // 회의록 등록 (AI 초안 상태로 최초 생성)
    void insertMom(VideoMomVO vo);

    // 회의록 내용 및 상태 수정
    void updateMom(VideoMomVO vo);

    // 회의록 수정 이력 단건 등록
    void insertMomHist(VideoMomHistVO vo);

    // 회의록 ID로 결재 목록 조회
    List<VideoMomAprvlVO> selectAprvlListByMomId(@Param("momId") Long momId);

    // 회의록 결재자 목록 일괄 등록
    void insertAprvlList(@Param("list") List<VideoMomAprvlVO> list);

    // 결재자 승인/반려 상태 수정
    void updateAprvl(VideoMomAprvlVO vo);

    // 화상 대화 로그 단건 등록
    void insertChatLog(VideoChatLogVO vo);

    // 녹취록 단건 등록
    void insertRcrdg(VideoRcrdgVO vo);
}
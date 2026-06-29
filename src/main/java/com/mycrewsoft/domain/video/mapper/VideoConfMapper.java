package com.mycrewsoft.domain.video.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.mycrewsoft.domain.video.vo.VideoConfVO;
import com.mycrewsoft.domain.video.vo.MeetingReminderVO;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;
import com.mycrewsoft.domain.video.vo.VideoPtcptLogVO;
import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;

@Mapper
public interface VideoConfMapper {

    // 화상회의 단건 등록 (roomNm은 "temp"로 INSERT 후 updateRoomNm으로 확정)
    void createVideoConf(VideoConfVO vo);

    // 화상회의 방 이름 확정
    void updateRoomNm(@Param("vconfId") Long vconfId, @Param("roomNm") String roomNm);

    // 화상회의 입퇴장 로그 등록
    void insertPtcptLog(VideoPtcptLogVO vo);

    // 입장 시각 업데이트 (LiveKit 실제 입장 시점)
    void updatePtcptLogJoinDt(@Param("ptcptLogId") Long ptcptLogId);

    // 퇴장 시각 업데이트 (LiveKit 실제 퇴장 시점)
    void updatePtcptLogLeavDt(@Param("ptcptLogId") Long ptcptLogId);

    // 퇴장 시간 찾기
    Long selectLatestOpenPtcptLogId(@Param("vconfId") Long vconfId, @Param("empId") Long empId);
    
    // 화상 대화 로그 단건 등록
    void insertChatLog(VideoChatLogVO vo);

    // 녹취록 단건 등록
    void insertRcrdg(VideoRcrdgVO vo);

    // 현재 사용자가 해당 회의 녹취 파일을 조회할 수 있는지 확인
    int countAccessibleRcrdg(
            @Param("vconfId") Long vconfId,
            @Param("atchFileDtlId") Long atchFileDtlId,
            @Param("empId") Long empId);

    // 회의 시작 10분 전 알림 (TB_MTNG 기준)
    List<MeetingReminderVO> selectConfsStartingSoon();

    // 대화 로그 조회
    List<VideoChatLogVO> selectChatLogsByVconfId(@Param("vconfId") Long vconfId);

}

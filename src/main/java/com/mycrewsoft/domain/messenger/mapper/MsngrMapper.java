package com.mycrewsoft.domain.messenger.mapper;

import com.mycrewsoft.domain.messenger.vo.MsngrChtrmVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgDetailVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmListVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmPtcptVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MsngrMapper {

    // 채팅방 목록 조회
	List<MsngrChtrmListVO> selectChtrmListByEmpId(
		@Param("empId") Long empId
	);
	
    // 채팅방 조회
    MsngrChtrmVO selectChtrmById(@Param("chtrmId") Long chtrmId);

    // 참여자 단건 조회
    MsngrChtrmPtcptVO selectPtcptByChtrmIdAndEmpId(
        @Param("chtrmId") Long chtrmId,
        @Param("empId") Long empId
    );

    // 메시지 조회
    MsngrMsgDetailVO selectMsgById(@Param("msgId") Long msgId);

    // 메시지 목록 조회
    List<MsngrMsgDetailVO> selectMsgListByChtrmId(@Param("chtrmId") Long chtrmId);

    // 채팅방 생성
    int insertChtrm(MsngrChtrmVO vo);

    // 참여자 등록
    int insertPtcpt(MsngrChtrmPtcptVO vo);

    // 채팅방 수정
    int updateChtrm(
        @Param("chtrmId") Long chtrmId,
        @Param("vo") MsngrChtrmVO vo
    );

    // 채팅방 삭제
    int deleteChtrm(@Param("chtrmId") Long chtrmId);

    // 참여자 삭제
    int leavePtcpt(
        @Param("chtrmId") Long chtrmId,
        @Param("empId") Long empId
    );

    // 참여자 재참여
    int rejoinPtcpt(
        @Param("chtrmId") Long chtrmId,
        @Param("empId") Long empId,
        @Param("ptcptSttusCd") String ptcptSttusCd
    );

    // 메시지 저장
    int insertMsg(MsngrMsgVO vo);

    // 참여자 상태 변경
    int updatePtcptSttus(@Param("empId") Long empId,
                         @Param("ptcptSttusCd") String ptcptSttusCd);

    // 사용자 상태 조회
    String selectPtcptSttus(@Param("empId") Long empId);

    // 사용자가 현재 참여 중인 채팅방 ID 조회
    List<Long> selectActiveChtrmIdsByEmpId(@Param("empId") Long empId);

    // 마지막 읽은 메시지 갱신
    int updateLastCfmtnMsgId(@Param("chtrmId") Long chtrmId,
                             @Param("empId") Long empId,
                             @Param("msgId") Long msgId);    
                 
    // 안 읽은 사용자 수 조회
    int selectUnreadCountByMsgId(@Param("chtrmId") Long chtrmId,
                                 @Param("msgId") Long msgId);
    
    // 프로젝트 채팅방 조회
    Long selectProjectChtrmId(@Param("projId") Long projId);
    
    // 프로젝트 인원 추가
    int mergeProjectPtcpt(MsngrChtrmPtcptVO ptcptVO);
    
    // 프로젝트 인원 퇴장 조치
    int updateProjectPtcptLeaveDt(
	    @Param("chtrmId") Long chtrmId,
	    @Param("empId") Long empId
	);
    
    // 위젯용 안 읽은 메시지 수만
    List<MsngrChtrmListVO> selectUnreadChtrmListByEmpId(
		@Param("empId") Long empId,
		@Param("limit") int limit
	);
}

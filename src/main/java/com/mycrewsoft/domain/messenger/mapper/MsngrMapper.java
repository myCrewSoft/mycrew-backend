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
    List<MsngrChtrmListVO> selectChtrmListByEmpId(@Param("empId") Long empId);

    // 채팅방(메시지 포함) 조회
    MsngrChtrmVO selectChtrmById(@Param("chtrmId") Long chtrmId);

    // 참여자 목록 조회
    List<MsngrChtrmPtcptVO> selectPtcptListByChtrmId(@Param("chtrmId") Long chtrmId);

    // 메시지 조회
    MsngrMsgDetailVO selectMsgById(@Param("msgId") Long msgId);

    // 메시지 목록 조회
    List<MsngrMsgDetailVO> selectMsgListByChtrmId(@Param("chtrmId") Long chtrmId);

    // 채팅방 생성
    int insertChtrm(MsngrChtrmVO vo);

    // 참여자 등록
    int insertPtcpt(MsngrChtrmPtcptVO vo);

    // 메시지 저장
    int insertMsg(MsngrMsgVO vo);

    // 참여자 상태 변경
    int updatePtcptSttus(@Param("empId") Long empId,
                         @Param("ptcptSttusCd") String ptcptSttusCd);

    // 사용자 상태 조회
    String selectPtcptSttus(@Param("empId") Long empId);

    // 마지막 읽은 메시지 갱신
    int updateLastCfmtnMsgId(@Param("chtrmId") Long chtrmId,
                              @Param("empId") Long empId,
                              @Param("msgId") Long msgId);                   
}
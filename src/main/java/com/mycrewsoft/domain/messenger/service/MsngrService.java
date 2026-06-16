package com.mycrewsoft.domain.messenger.service;

import java.util.List;

import com.mycrewsoft.domain.messenger.dto.request.AddParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.RemoveParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.enums.ParticipantStatus;

public interface MsngrService {
    
    // Rest용
    List<ChatRoomResponse> getChtrmList();

    ChatRoomResponse getChtrm(Long chtrmId);

    List<ChatMessageResponse> getMsgList(Long chtrmId);

    Long createChtrm(CreateChatRoomRequest request);

    void updatePtcptSttus(ParticipantStatus status);

    void updatePtcptSttusById(Long empId, ParticipantStatus status);

    void updateLastCfmtnMsgId(Long chtrmId, Long msgId);

    void updateChtrm(Long chtrmId, UpdateChatRoomRequest request);

    void deleteChtrm(Long chtrmId);

    void addChtrmPtcpt(Long chtrmId, AddParticipantsRequest request);

    void removeChtrmPtcpt(Long chtrmId, RemoveParticipantsRequest request);

    // 프로젝트 채팅방 생성
    Long createProjectChtrm(
		Long projId,
        String projNm,
        Long crtrId,
        List<Long> empIds
    );
   
    // 프로젝트 인원 추가시 초대
    void addProjectChtrmParticipants(Long chtrmId, List<Long> empIds);
    
    // 프로젝트 인원 삭제 시 퇴장 조치
    void removeProjectChtrmParticipant(Long chtrmId, Long empId);
    
    // 웹소켓용
    void saveMsgAndBroadcast(Long chtrmId, String content, Long sndrId);

}

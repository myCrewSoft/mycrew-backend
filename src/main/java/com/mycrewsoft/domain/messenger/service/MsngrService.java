package com.mycrewsoft.domain.messenger.service;

import java.util.List;

import com.mycrewsoft.domain.messenger.dto.request.AddParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.RemoveParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;

public interface MsngrService {
    
    // Rest용
    List<ChatRoomResponse> getChtrmList();

    ChatRoomResponse getChtrm(Long chtrmId);

    List<ChatMessageResponse> getMsgList(Long chtrmId);

    Long createChtrm(CreateChatRoomRequest request);

    void updatePtcptSttus(String ptcptSttusCd);

    void updatePtcptSttusById(Long empId, String ptcptSttusCd);

    void updateLastCfmtnMsgId(Long chtrmId, Long msgId);

    void updateChtrm(Long chtrmId, UpdateChatRoomRequest request);

    void deleteChtrm(Long chtrmId);

    void addChtrmPtcpt(Long chtrmId, AddParticipantsRequest request);

    void removeChtrmPtcpt(Long chtrmId, RemoveParticipantsRequest request);

    // 웹소켓용
    void saveMsgAndBroadcast(Long chtrmId, String content, Long sndrId);

}

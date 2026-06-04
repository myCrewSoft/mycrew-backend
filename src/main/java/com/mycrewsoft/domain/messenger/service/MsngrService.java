package com.mycrewsoft.domain.messenger.service;

import java.util.List;

import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;

public interface MsngrService {
    
    List<ChatRoomResponse> getChtrmList();

    ChatRoomResponse getChtrm(Long chtrmId);

    List<ChatMessageResponse> getMsgList(Long chtrmId);

    Long createChtrm(CreateChatRoomRequest request);

    void saveMsgAndBroadcast(Long chtrmId, String content, Long sndrId);

    void updatePtcptSttus(String ptcptSttusCd);

    void updatePtcptSttusById(Long empId, String ptcptSttusCd);

    void updateLastCfmtnMsgId(Long chtrmId, Long msgId);
}

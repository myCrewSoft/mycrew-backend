package com.mycrewsoft.domain.messenger.mapper;

import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmListVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MsngrDtoMapper {

    @Mapping(source = "chtrmId",     target = "id")
    @Mapping(source = "chtrmNm",     target = "name")
    @Mapping(source = "chtrmExpln",  target = "description")
    @Mapping(source = "chtrmTypeCd", target = "type")
    @Mapping(source = "chtrmImgAtchFileId", target = "chatRoomImageAtchFileId")
    @Mapping(source = "participantCount", target = "participantCount")
    @Mapping(target = "participants", ignore = true)
    ChatRoomResponse toRoomResponse(MsngrChtrmVO vo);

    @Mapping(source = "chtrmId",     target = "id")
    @Mapping(source = "chtrmNm",     target = "name")
    @Mapping(source = "chtrmExpln",  target = "description")
    @Mapping(source = "chtrmTypeCd", target = "type")
    @Mapping(source = "lastMessage", target = "lastMessage")
    @Mapping(source = "lastTime",    target = "lastTime")
    @Mapping(source = "unreadCount", target = "unreadCount")
    @Mapping(source = "prflImgFileId", target = "prflImgFileId")
    @Mapping(source = "chtrmImgAtchFileId", target = "chatRoomImageAtchFileId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "jobTitle", target = "jobTitle")
    @Mapping(source = "department", target = "department")
    @Mapping(source = "participantCount", target = "participantCount")
    @Mapping(target = "participants", ignore = true)
    ChatRoomResponse toRoomResponseFromList(MsngrChtrmListVO vo);

    List<ChatRoomResponse> toRoomResponseListFromList(List<MsngrChtrmListVO> voList);

    @Mapping(source = "msgId",     target = "id")
    @Mapping(source = "chtrmId",   target = "chatRoomId")
    @Mapping(source = "sndrId",     target = "senderId")
    @Mapping(source = "msgCn",     target = "content")
    @Mapping(source = "creatDt",   target = "time", dateFormat = "HH:mm")
    @Mapping(source = "senderName", target = "senderName")
    @Mapping(target = "mine", ignore = true)
    @Mapping(target = "read", ignore = true)
    ChatMessageResponse toResponseFromDetail(MsngrMsgDetailVO vo);

    @Mapping(source = "chatName",        target = "chtrmNm")
    @Mapping(source = "chatDescription", target = "chtrmExpln")
    @Mapping(source = "chatRoomImageAtchFileId", target = "chtrmImgAtchFileId")
    @Mapping(target = "chtrmId",     ignore = true)
    @Mapping(target = "chtrmTypeCd", ignore = true)
    @Mapping(target = "estblshId",   ignore = true)
    @Mapping(target = "creatDt",     ignore = true)
    @Mapping(target = "endDt",       ignore = true)
    @Mapping(target = "msngrChtrmPtcpt", ignore = true)
    MsngrChtrmVO toChtrmVO(CreateChatRoomRequest request);

    @Mapping(source = "chatName",        target = "chtrmNm")
    @Mapping(source = "chatDescription", target = "chtrmExpln")
    @Mapping(source = "chatRoomImageAtchFileId", target = "chtrmImgAtchFileId")
    @Mapping(target = "chtrmId",     ignore = true)
    @Mapping(target = "chtrmTypeCd", ignore = true)
    @Mapping(target = "estblshId",   ignore = true)
    @Mapping(target = "creatDt",     ignore = true)
    @Mapping(target = "endDt",       ignore = true)
    @Mapping(target = "msngrChtrmPtcpt", ignore = true)
    MsngrChtrmVO toChtrmVO(UpdateChatRoomRequest request);
}

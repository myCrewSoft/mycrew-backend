package com.mycrewsoft.domain.messenger.mapper;

import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmListVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgDetailVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MsngrDtoMapper {

    @Mapping(source = "chtrmId",     target = "id")
    @Mapping(source = "chtrmNm",     target = "name")
    @Mapping(source = "chtrmExpln",  target = "description")
    @Mapping(source = "chtrmTypeCd", target = "type")
    ChatRoomResponse toRoomResponse(MsngrChtrmVO vo);

    List<ChatRoomResponse> toRoomResponseList(List<MsngrChtrmVO> voList);

    @Mapping(source = "msgId",   target = "id")
    @Mapping(source = "msgCn",   target = "content")
    @Mapping(source = "creatDt", target = "time", dateFormat = "HH:mm")
    @Mapping(target = "mine", ignore = true)
    @Mapping(target = "read", ignore = true)
    ChatMessageResponse toMessageResponse(MsngrMsgVO vo);

    List<ChatMessageResponse> toMessageResponseList(List<MsngrMsgVO> voList);

    @Mapping(source = "chtrmId",     target = "id")
    @Mapping(source = "chtrmNm",     target = "name")
    @Mapping(source = "chtrmExpln",  target = "description")
    @Mapping(source = "chtrmTypeCd", target = "type")
    ChatRoomResponse toRoomResponseFromList(MsngrChtrmListVO vo);

    List<ChatRoomResponse> toRoomResponseListFromList(List<MsngrChtrmListVO> voList);

    @Mapping(source = "msgId",     target = "id")
    @Mapping(source = "sndrId",     target = "senderId")
    @Mapping(source = "msgCn",     target = "content")
    @Mapping(source = "creatDt",   target = "time", dateFormat = "HH:mm")
    @Mapping(source = "senderName", target = "senderName")
    @Mapping(target = "mine", ignore = true)
    @Mapping(target = "read", ignore = true)
    ChatMessageResponse toResponseFromDetail(MsngrMsgDetailVO vo);

    @Mapping(source = "chatName",        target = "chtrmNm")
    @Mapping(source = "chatDescription", target = "chtrmExpln")
    @Mapping(target = "chtrmId",     ignore = true)
    @Mapping(target = "chtrmTypeCd", ignore = true)
    @Mapping(target = "estblshId",   ignore = true)
    @Mapping(target = "creatDt",     ignore = true)
    @Mapping(target = "endDt",       ignore = true)
    @Mapping(target = "msngrChtrmPtcpt", ignore = true)
    @Mapping(target = "msngrMsg",        ignore = true)
    MsngrChtrmVO toChtrmVO(CreateChatRoomRequest request);
}
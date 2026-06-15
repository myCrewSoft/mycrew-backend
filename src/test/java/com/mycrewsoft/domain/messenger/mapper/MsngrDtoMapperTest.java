package com.mycrewsoft.domain.messenger.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmListVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmVO;

class MsngrDtoMapperTest {

    private final MsngrDtoMapper mapper = Mappers.getMapper(MsngrDtoMapper.class);

    @Test
    void createRequest_mapsChatRoomImageId() {
        CreateChatRoomRequest request = new CreateChatRoomRequest();
        request.setChatRoomImageAtchFileId(10L);

        MsngrChtrmVO result = mapper.toChtrmVO(request);

        assertThat(result.getChtrmImgAtchFileId()).isEqualTo(10L);
    }

    @Test
    void updateRequest_mapsChatRoomImageId() {
        UpdateChatRoomRequest request = new UpdateChatRoomRequest();
        request.setChatRoomImageAtchFileId(20L);

        MsngrChtrmVO result = mapper.toChtrmVO(request);

        assertThat(result.getChtrmImgAtchFileId()).isEqualTo(20L);
    }

    @Test
    void roomDetail_mapsChatRoomImageId() {
        MsngrChtrmVO vo = new MsngrChtrmVO();
        vo.setChtrmImgAtchFileId(30L);

        ChatRoomResponse result = mapper.toRoomResponse(vo);

        assertThat(result.getChatRoomImageAtchFileId()).isEqualTo(30L);
    }

    @Test
    void roomList_mapsChatRoomImageId() {
        MsngrChtrmListVO vo = new MsngrChtrmListVO();
        vo.setChtrmImgAtchFileId(40L);

        ChatRoomResponse result = mapper.toRoomResponseFromList(vo);

        assertThat(result.getChatRoomImageAtchFileId()).isEqualTo(40L);
    }
}

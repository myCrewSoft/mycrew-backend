package com.mycrewsoft.domain.messenger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.messenger.dto.request.AddParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.RemoveParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatEventResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.dto.response.ParticipantChangedResponse;
import com.mycrewsoft.domain.messenger.dto.response.ParticipantStatusResponse;
import com.mycrewsoft.domain.messenger.dto.response.ReadChangedResponse;
import com.mycrewsoft.domain.messenger.enums.ChatEventType;
import com.mycrewsoft.domain.messenger.enums.ParticipantStatus;
import com.mycrewsoft.domain.messenger.mapper.MsngrDtoMapper;
import com.mycrewsoft.domain.messenger.mapper.MsngrMapper;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmListVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmPtcptVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class MsngrServiceImplTest {

    @Mock
    private MsngrMapper msngrMapper;

    @Mock
    private MsngrDtoMapper msngrDtoMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private MsngrServiceImpl msngrService;

    @Test
    void getChtrmList_returnsCurrentUserRooms() {
        Long empId = 1001L;
        MsngrChtrmListVO vo = new MsngrChtrmListVO();
        ChatRoomResponse response = ChatRoomResponse.builder()
                .id(1L)
                .name("개발팀")
                .build();

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmListByEmpId(empId)).thenReturn(List.of(vo));
            when(msngrDtoMapper.toRoomResponseListFromList(List.of(vo))).thenReturn(List.of(response));

            List<ChatRoomResponse> result = msngrService.getChtrmList();

            assertThat(result).containsExactly(response);
            verify(msngrMapper).selectChtrmListByEmpId(empId);
        }
    }

    @Test
    void getChtrm_whenActiveParticipant_returnsRoom() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", 1001L, activePtcpt(chtrmId, empId));
        ChatRoomResponse response = ChatRoomResponse.builder()
                .id(chtrmId)
                .name("개발팀")
                .build();

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);
            when(msngrDtoMapper.toRoomResponse(chtrm)).thenReturn(response);

            ChatRoomResponse result = msngrService.getChtrm(chtrmId);

            assertThat(result.getId()).isEqualTo(chtrmId);
            assertThat(result.getName()).isEqualTo("개발팀");
            assertThat(result.getParticipantCount()).isEqualTo(1);
            assertThat(result.getParticipants()).hasSize(1);
            assertThat(result.getParticipants().get(0).getEmpId()).isEqualTo(empId);
        }
    }

    @Test
    void getChtrm_whenParticipantAlreadyLeft_throwsNotParticipant() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", 2001L, leftPtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);

            assertThatThrownBy(() -> msngrService.getChtrm(chtrmId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CHAT_NOT_PARTICIPANT);
        }
    }

    @Test
    void createChtrm_createsRoomAndParticipants() {
        Long empId = 1001L;
        Long chtrmId = 10L;
        CreateChatRoomRequest request = new CreateChatRoomRequest();
        request.setChatName("개발팀");
        request.setChatDescription("개발팀 채팅방");
        request.setParticipantIds(List.of(2001L, 2002L));

        MsngrChtrmVO chtrmVO = new MsngrChtrmVO();

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectPtcptSttus(empId)).thenReturn(ParticipantStatus.ONLINE.getCode());
            when(msngrMapper.selectPtcptSttus(2001L)).thenReturn(ParticipantStatus.OFFLINE.getCode());
            when(msngrMapper.selectPtcptSttus(2002L)).thenReturn(null);
            when(msngrDtoMapper.toChtrmVO(request)).thenReturn(chtrmVO);
            doAnswer(invocation -> {
                MsngrChtrmVO vo = invocation.getArgument(0);
                vo.setChtrmId(chtrmId);
                return 1;
            }).when(msngrMapper).insertChtrm(chtrmVO);

            Long result = msngrService.createChtrm(request);

            assertThat(result).isEqualTo(chtrmId);
            assertThat(chtrmVO.getChtrmTypeCd()).isEqualTo("M2");
            assertThat(chtrmVO.getEstblshId()).isEqualTo(empId);
            verify(msngrMapper).insertChtrm(chtrmVO);
            verify(msngrMapper).updatePtcptSttus(empId, ParticipantStatus.ONLINE.getCode());
            verify(msngrMapper, org.mockito.Mockito.times(3)).insertPtcpt(any(MsngrChtrmPtcptVO.class));
        }
    }

    @Test
    void updateChtrm_whenOwner_updatesRoom() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        UpdateChatRoomRequest request = new UpdateChatRoomRequest();
        request.setChatName("수정된 채팅방");
        request.setChatDescription("수정된 설명");
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", empId, activePtcpt(chtrmId, empId));
        MsngrChtrmVO updateVO = new MsngrChtrmVO();

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);
            when(msngrDtoMapper.toChtrmVO(request)).thenReturn(updateVO);
            when(msngrMapper.updateChtrm(chtrmId, updateVO)).thenReturn(1);

            msngrService.updateChtrm(chtrmId, request);

            verify(msngrMapper).updateChtrm(chtrmId, updateVO);
        }
    }

    @Test
    void updateChtrm_whenNotOwner_throwsAccessDenied() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        UpdateChatRoomRequest request = new UpdateChatRoomRequest();
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", 2001L, activePtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);

            assertThatThrownBy(() -> msngrService.updateChtrm(chtrmId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
            verify(msngrMapper, never()).updateChtrm(any(), any());
        }
    }

    @Test
    void deleteChtrm_whenOwner_deletesRoom() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", empId, activePtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);
            when(msngrMapper.deleteChtrm(chtrmId)).thenReturn(1);

            msngrService.deleteChtrm(chtrmId);

            verify(msngrMapper).deleteChtrm(chtrmId);
        }
    }

    @Test
    void addChtrmPtcpt_insertsNewParticipantAndRejoinsLeftParticipant() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        AddParticipantsRequest request = new AddParticipantsRequest();
        request.setParticipantIds(List.of(2001L, 2002L, 2003L));

        MsngrChtrmVO chtrm = chtrm(
                chtrmId,
                "M2",
                empId,
                activePtcpt(chtrmId, empId),
                leftPtcpt(chtrmId, 2001L),
                activePtcpt(chtrmId, 2003L)
        );

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);
            when(msngrMapper.selectPtcptSttus(2001L)).thenReturn(ParticipantStatus.OFFLINE.getCode());
            when(msngrMapper.selectPtcptSttus(2002L)).thenReturn(ParticipantStatus.ONLINE.getCode());
            when(msngrMapper.selectPtcptByChtrmIdAndEmpId(chtrmId, 2001L)).thenReturn(leftPtcpt(chtrmId, 2001L));
            when(msngrMapper.selectPtcptByChtrmIdAndEmpId(chtrmId, 2002L)).thenReturn(null);

            msngrService.addChtrmPtcpt(chtrmId, request);

            verify(msngrMapper).rejoinPtcpt(
                    chtrmId,
                    2001L,
                    ParticipantStatus.OFFLINE.getCode()
            );
            verify(msngrMapper).insertPtcpt(any(MsngrChtrmPtcptVO.class));
            verify(msngrMapper, never()).selectPtcptSttus(2003L);

            ChatEventResponse<?> event = captureEvent("/topic/chats/" + chtrmId + "/events");
            assertThat(event.getEventType()).isEqualTo(ChatEventType.PARTICIPANT_ADDED);
            ParticipantChangedResponse data = (ParticipantChangedResponse) event.getData();
            assertThat(data.getChatRoomId()).isEqualTo(chtrmId);
            assertThat(data.getParticipantIds()).containsExactly(2001L, 2002L);
        }
    }

    @Test
    void addChtrmPtcpt_whenDirectRoom_throwsCannotAddParticipant() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        AddParticipantsRequest request = new AddParticipantsRequest();
        request.setParticipantIds(List.of(2001L));
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M1", empId, activePtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);

            assertThatThrownBy(() -> msngrService.addChtrmPtcpt(chtrmId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CHAT_DIRECT_ROOM_CANNOT_ADD_PARTICIPANT);
            verify(msngrMapper, never()).insertPtcpt(any());
        }
    }

    @Test
    void removeChtrmPtcpt_whenOwner_removesParticipantsAndSendsEvent() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        RemoveParticipantsRequest request = new RemoveParticipantsRequest();
        request.setParticipantIds(List.of(2001L, 2002L));
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", empId, activePtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);
            when(msngrMapper.leavePtcpt(chtrmId, 2001L)).thenReturn(1);
            when(msngrMapper.leavePtcpt(chtrmId, 2002L)).thenReturn(1);

            msngrService.removeChtrmPtcpt(chtrmId, request);

            verify(msngrMapper).leavePtcpt(chtrmId, 2001L);
            verify(msngrMapper).leavePtcpt(chtrmId, 2002L);

            ChatEventResponse<?> event = captureEvent("/topic/chats/" + chtrmId + "/events");
            assertThat(event.getEventType()).isEqualTo(ChatEventType.PARTICIPANT_REMOVED);
            ParticipantChangedResponse data = (ParticipantChangedResponse) event.getData();
            assertThat(data.getParticipantIds()).containsExactly(2001L, 2002L);
        }
    }

    @Test
    void removeChtrmPtcpt_whenRemovingOwner_throwsCannotRemoveOwner() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        RemoveParticipantsRequest request = new RemoveParticipantsRequest();
        request.setParticipantIds(List.of(empId));
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", empId, activePtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);

            assertThatThrownBy(() -> msngrService.removeChtrmPtcpt(chtrmId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CHAT_OWNER_CANNOT_BE_REMOVED);
            verify(msngrMapper, never()).leavePtcpt(any(), any());
        }
    }

    @Test
    void updatePtcptSttus_updatesStatusAndSendsStatusEvent() {
        Long empId = 1001L;
        Long chtrmId = 1L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectActiveChtrmIdsByEmpId(empId)).thenReturn(List.of(chtrmId));

            msngrService.updatePtcptSttus(ParticipantStatus.AWAY);

            verify(msngrMapper).updatePtcptSttus(empId, ParticipantStatus.AWAY.getCode());
            ChatEventResponse<?> event = captureEvent("/topic/chats/status");
            assertThat(event.getEventType()).isEqualTo(ChatEventType.PARTICIPANT_STATUS_CHANGED);
            ParticipantStatusResponse data = (ParticipantStatusResponse) event.getData();
            assertThat(data.getEmpId()).isEqualTo(empId);
            assertThat(data.getPtcptSttusCd()).isEqualTo(ParticipantStatus.AWAY.getCode());
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/chats/" + chtrmId + "/events"),
                    any(ChatEventResponse.class)
            );
        }
    }

    @Test
    void updateLastCfmtnMsgId_updatesReadPositionAndSendsReadEvent() {
        Long empId = 1001L;
        Long chtrmId = 1L;
        Long msgId = 101L;
        MsngrChtrmVO chtrm = chtrm(chtrmId, "M2", 2001L, activePtcpt(chtrmId, empId));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            when(msngrMapper.selectChtrmById(chtrmId)).thenReturn(chtrm);
            when(msngrMapper.selectUnreadCountByMsgId(chtrmId, msgId)).thenReturn(2);

            msngrService.updateLastCfmtnMsgId(chtrmId, msgId);

            verify(msngrMapper).updateLastCfmtnMsgId(chtrmId, empId, msgId);
            ChatEventResponse<?> event = captureEvent("/topic/chats/" + chtrmId + "/events");
            assertThat(event.getEventType()).isEqualTo(ChatEventType.READ_CHANGED);
            ReadChangedResponse data = (ReadChangedResponse) event.getData();
            assertThat(data.getChatRoomId()).isEqualTo(chtrmId);
            assertThat(data.getEmpId()).isEqualTo(empId);
            assertThat(data.getMessageId()).isEqualTo(msgId);
            assertThat(data.getUnreadCount()).isEqualTo(2);
        }
    }

    private ChatEventResponse<?> captureEvent(String destination) {
        ArgumentCaptor<ChatEventResponse> captor = ArgumentCaptor.forClass(ChatEventResponse.class);
        verify(messagingTemplate).convertAndSend(eq(destination), captor.capture());
        return captor.getValue();
    }

    private MsngrChtrmVO chtrm(Long chtrmId, String chtrmTypeCd, Long estblshId, MsngrChtrmPtcptVO... participants) {
        MsngrChtrmVO chtrm = new MsngrChtrmVO();
        chtrm.setChtrmId(chtrmId);
        chtrm.setChtrmTypeCd(chtrmTypeCd);
        chtrm.setEstblshId(estblshId);
        chtrm.setMsngrChtrmPtcpt(List.of(participants));
        return chtrm;
    }

    private MsngrChtrmPtcptVO activePtcpt(Long chtrmId, Long empId) {
        MsngrChtrmPtcptVO ptcpt = new MsngrChtrmPtcptVO();
        ptcpt.setChtrmId(chtrmId);
        ptcpt.setEmpId(empId);
        ptcpt.setPtcptSttusCd(ParticipantStatus.ONLINE.getCode());
        return ptcpt;
    }

    private MsngrChtrmPtcptVO leftPtcpt(Long chtrmId, Long empId) {
        MsngrChtrmPtcptVO ptcpt = activePtcpt(chtrmId, empId);
        ptcpt.setLeavDt(LocalDateTime.now());
        return ptcpt;
    }
}

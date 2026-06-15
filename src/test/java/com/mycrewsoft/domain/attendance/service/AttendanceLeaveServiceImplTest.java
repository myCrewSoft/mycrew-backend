package com.mycrewsoft.domain.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.service.ApprovalDraftWriteService;
import com.mycrewsoft.domain.approval.service.ApprovalRequestService;
import com.mycrewsoft.domain.attendance.dto.request.OtApplyRequest;
import com.mycrewsoft.domain.attendance.dto.response.MyOtResponse;
import com.mycrewsoft.domain.attendance.mapper.AttendanceMapper;
import com.mycrewsoft.domain.attendance.vo.AtndOtReqVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class AttendanceLeaveServiceImplTest {

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private ApprovalDraftWriteService approvalDraftWriteService;

    @Mock
    private ApprovalRequestService approvalRequestService;

    @Mock
    private AuthorizationService authorizationService;

    @Test
    void applyOtCreatesApprovalDraftStoresRequestAndSubmitsApproval() {
        AttendanceLeaveServiceImpl service = newService();
        Long empId = 1234L;
        Long drftDocSn = 77L;
        OtApplyRequest request = new OtApplyRequest();
        request.setOtYmd(LocalDate.of(2026, 6, 20));
        request.setOtBgnTm("18:00");
        request.setOtEndTm("21:30");
        request.setReqRsn("배포 대응");
        request.setDocTtl("초과근무 신청서");
        request.setTmplatCd("OT_REQ");
        request.setAprvlFullCn("<p>초과근무</p>");
        request.setAprvlHopeDt(LocalDateTime.of(2026, 6, 19, 18, 0));
        request.setAtchFileId(9L);
        request.setApprovalLines(List.of(approvalStep()));

        when(approvalDraftWriteService.saveTemporaryDraft(any(ApprovalDraftRequestDTO.class)))
                .thenReturn(drftDocSn);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            Long result = service.applyOt(request);

            assertThat(result).isEqualTo(drftDocSn);
        }

        ArgumentCaptor<ApprovalDraftRequestDTO> draftCaptor =
                ArgumentCaptor.forClass(ApprovalDraftRequestDTO.class);
        verify(approvalDraftWriteService).saveTemporaryDraft(draftCaptor.capture());
        assertThat(draftCaptor.getValue().getDocTtl()).isEqualTo("초과근무 신청서");
        assertThat(draftCaptor.getValue().getTmplatCd()).isEqualTo("OT_REQ");

        ArgumentCaptor<AtndOtReqVO> otCaptor = ArgumentCaptor.forClass(AtndOtReqVO.class);
        verify(attendanceMapper).insertOtReq(otCaptor.capture());
        assertThat(otCaptor.getValue().getEmpId()).isEqualTo(empId);
        assertThat(otCaptor.getValue().getDrftDocSn()).isEqualTo(drftDocSn);
        assertThat(otCaptor.getValue().getOtDt()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(otCaptor.getValue().getOtBgnDtm()).isEqualTo(LocalDateTime.of(2026, 6, 20, 18, 0));
        assertThat(otCaptor.getValue().getOtEndDtm()).isEqualTo(LocalDateTime.of(2026, 6, 20, 21, 30));
        verify(approvalRequestService).submitApproval(drftDocSn);
    }

    @Test
    void reflectApprovedDocumentReflectsOvertimeWhenLeaveRequestDoesNotExist() {
        AttendanceLeaveServiceImpl service = newService();
        Long drftDocSn = 77L;
        AtndOtReqVO otReq = new AtndOtReqVO();
        otReq.setOtReqId(10L);
        otReq.setEmpId(1234L);
        otReq.setOtDt(LocalDate.of(2026, 6, 20));
        otReq.setOtBgnDtm(LocalDateTime.of(2026, 6, 20, 18, 0));
        otReq.setOtEndDtm(LocalDateTime.of(2026, 6, 20, 21, 30));
        otReq.setRflctYn("N");

        when(attendanceMapper.selectLeaveReqByDoc(drftDocSn)).thenReturn(null);
        when(attendanceMapper.selectOtReqByDoc(drftDocSn)).thenReturn(otReq);
        when(attendanceMapper.selectApprovedOtMin(1234L, LocalDate.of(2026, 6, 20))).thenReturn(210);

        service.reflectApprovedDocument(drftDocSn);

        verify(attendanceMapper).updateOtReqReflected(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(210),
                any(LocalDateTime.class));
        verify(attendanceMapper).updateDailyApprovedOt(1234L, LocalDate.of(2026, 6, 20), 210);
    }

    @Test
    void getMyOtsReturnsCurrentUsersOvertimeRequests() {
        AttendanceLeaveServiceImpl service = newService();
        Long empId = 1234L;
        List<MyOtResponse> expected = List.of(new MyOtResponse());
        when(attendanceMapper.selectMyOts(empId)).thenReturn(expected);

        List<MyOtResponse> result;
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            result = service.getMyOts();
        }

        assertThat(result).isSameAs(expected);
    }

    private AttendanceLeaveServiceImpl newService() {
        return new AttendanceLeaveServiceImpl(
                attendanceMapper,
                approvalDraftWriteService,
                approvalRequestService,
                authorizationService);
    }

    private ApprovalStepRequestDTO approvalStep() {
        ApprovalStepRequestDTO step = new ApprovalStepRequestDTO();
        step.setAprvlMthdCd("01");
        step.setAprvlOrd(1L);
        step.setAprvrEmpIds(List.of(2001L));
        return step;
    }
}

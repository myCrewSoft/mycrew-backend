package com.mycrewsoft.domain.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.event.ApprovalRequestedEvent;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class ApprovalRequestServiceImplTest {

    @Mock
    private ApprovalServiceSupport support;

    @Mock
    private ApprovalDraftMapper approvalDraftMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmployeeMapper employeeMapper;

    @Test
    void submitApprovalPublishesEventWhenLoadedStepHasNoNestedLines() {
        Long drafterEmpId = 1234L;
        Long drftDocSn = 40L;
        ApprovalRequestServiceImpl service = new ApprovalRequestServiceImpl(
                support,
                approvalDraftMapper,
                eventPublisher,
                employeeMapper
        );
        ApprovalDocVO savedDoc = new ApprovalDocVO();
        savedDoc.setDrftDocSn(drftDocSn);
        savedDoc.setEmpId(drafterEmpId);
        savedDoc.setDocTtl("휴가 신청서");
        ApprovalStepVO firstStep = new ApprovalStepVO();
        firstStep.setAprvlStepSn(100L);
        firstStep.setApprovalLines(null);
        EmployeeProfileDTO profile = new EmployeeProfileDTO();
        profile.setEmpNm("홍길동");

        when(support.requireDocForUpdate(drftDocSn)).thenReturn(savedDoc);
        when(approvalDraftMapper.selectFirstApprovalStep(drftDocSn)).thenReturn(firstStep);
        when(approvalDraftMapper.selectApproverEmpIdsByStep(100L)).thenReturn(java.util.List.of(2001L, 2002L));
        when(employeeMapper.selectEmployeeProfileByEmpId(drafterEmpId)).thenReturn(profile);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(drafterEmpId);

            assertThatCode(() -> service.submitApproval(drftDocSn)).doesNotThrowAnyException();
        }

        ArgumentCaptor<ApprovalRequestedEvent> eventCaptor = ArgumentCaptor.forClass(ApprovalRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getApproverIds()).containsExactly(2001L, 2002L);
    }
}

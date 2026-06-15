package com.mycrewsoft.common.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mycrewsoft.domain.approval.event.ApprovalApprovedEvent;
import com.mycrewsoft.domain.approval.event.ApprovalCancelledEvent;
import com.mycrewsoft.domain.approval.event.ApprovalDeadlineEvent;
import com.mycrewsoft.domain.approval.event.ApprovalRejectedEvent;
import com.mycrewsoft.domain.approval.event.ApprovalRequestedEvent;
import com.mycrewsoft.domain.board.event.CommentCreatedEvent;
import com.mycrewsoft.domain.board.event.NoticeCreatedEvent;
import com.mycrewsoft.domain.board.event.PostDeletedByAdminEvent;
import com.mycrewsoft.domain.education.event.EducationCancelledEvent;
import com.mycrewsoft.domain.education.event.EducationCompletedEvent;
import com.mycrewsoft.domain.education.event.EducationDeadlineEvent;
import com.mycrewsoft.domain.education.event.EducationEnrolledEvent;
import com.mycrewsoft.domain.education.event.EducationStartedEvent;
import com.mycrewsoft.domain.employee.event.FirstLoginEvent;
import com.mycrewsoft.domain.employee.event.PasswordChangedEvent;
import com.mycrewsoft.domain.employee.event.PermissionChangedEvent;
import com.mycrewsoft.domain.employee.event.ProfileChangedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingChangedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingEndedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingInvitedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingReminderEvent;
import com.mycrewsoft.domain.notification.service.NotificationService;
import com.mycrewsoft.domain.project.event.ProjectClosedEvent;
import com.mycrewsoft.domain.project.event.ProjectCompletedEvent;
import com.mycrewsoft.domain.project.event.ProjectCreatedEvent;
import com.mycrewsoft.domain.project.event.ProjectDeadlineEvent;
import com.mycrewsoft.domain.project.event.ProjectManagerChangedEvent;
import com.mycrewsoft.domain.project.event.ProjectMemberRemovedEvent;
import com.mycrewsoft.domain.project.event.ProjectMembersAddedEvent;
import com.mycrewsoft.domain.project.event.ProjectStoppedEvent;
import com.mycrewsoft.domain.schedule.event.ScheduleCancelledEvent;
import com.mycrewsoft.domain.schedule.event.ScheduleChangedEvent;
import com.mycrewsoft.domain.schedule.event.ScheduleCreatedEvent;
import com.mycrewsoft.domain.schedule.event.ScheduleReminderEvent;
import com.mycrewsoft.domain.task.event.TaskAssignedEvent;
import com.mycrewsoft.domain.task.event.TaskCancelledEvent;
import com.mycrewsoft.domain.task.event.TaskCompletedEvent;
import com.mycrewsoft.domain.task.event.TaskDeadlineChangedEvent;
import com.mycrewsoft.domain.task.event.TaskDeadlineEvent;
import com.mycrewsoft.domain.task.event.TaskManagerChangedEvent;
import com.mycrewsoft.domain.task.event.TaskMemberRemovedEvent;
import com.mycrewsoft.domain.task.event.TaskStatusChangedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    // 첫로그인

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFirstLogin(FirstLoginEvent event) {
        notificationService.sendAlrm(
            "입사를 축하드립니다. 그룹웨어 이용을 시작해 주세요.",
            "01",
            null,
            List.of(event.getEmpId())
        );
    }

    // 전자결재

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalApproved(ApprovalApprovedEvent event) {
        notificationService.sendAlrm(
            "신청하신 [" + event.getApprovalNm() + "] 결재가 승인되었습니다.",
            "02",
            null,
            List.of(event.getRcvrEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalRejected(ApprovalRejectedEvent event) {
        notificationService.sendAlrm(
            "신청하신 [" + event.getApprovalNm() + "] 결재가 반려되었습니다.",
            "02",
            null,
            List.of(event.getRcvrEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalRequested(ApprovalRequestedEvent event) {
        notificationService.sendAlrm(
            event.getApplicantNm() + " 님이 [" + event.getApprovalNm() + "] 결재를 요청했습니다.",
            "02",
            null,
            event.getApproverIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalDeadline(ApprovalDeadlineEvent event) {
        notificationService.sendAlrm(
            "[" + event.getApprovalNm() + "] 결재의 처리 기한이 임박했습니다.(3일 전)",
            "02",
            null,
            List.of(event.getApproverId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalCancelled(ApprovalCancelledEvent event) {
        notificationService.sendAlrm(
            event.getApplicantNm() + " 님이 [" + event.getApprovalNm() + "] 결재 요청을 취소했습니다.",
            "02",
            null,
            event.getApproverIds()
        );
    }

    // 프로젝트

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreated(ProjectCreatedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getProjNm() + "] 프로젝트에 등록되었습니다.",
            "04",
            null,
            event.getEmpIds()
        );
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectMembersAdded(ProjectMembersAddedEvent event) {
    	notificationService.sendAlrm(
    			"[" + event.getProjNm() + "] 프로젝트에 등록되었습니다.",
    			"04",
    			null,
    			event.getEmpIds()
    			);
    }
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectMemberRemoved(ProjectMemberRemovedEvent event) {
    	notificationService.sendAlrm(
    			"[" + event.getProjNm() + "] 프로젝트에서 제외되었습니다.",
    			"04",
    			null,
    			List.of(event.getEmpId())
    			);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectDeadline(ProjectDeadlineEvent event) {
        notificationService.sendAlrm(
            "[" + event.getProjNm() + "] 프로젝트의 마감일이 하루 남았습니다.",
            "04",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectClosed(ProjectClosedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getProjNm() + "] 프로젝트가 마감되었습니다.",
            "04",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCancelled(ProjectStoppedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getProjNm() + "] 프로젝트가 중지되었습니다.",
            "04",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectManagerChanged(ProjectManagerChangedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getProjNm() + "] 프로젝트의 담당자가 [" + event.getManagerNm() + "] 으로 변경되었습니다.",
            "04",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCompleted(ProjectCompletedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getProjNm() + "] 프로젝트가 완료 처리되었습니다.",
            "04",
            null,
            event.getEmpIds()
        );
    }

    // 업무

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무가 배정되었습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMemberRemoved(TaskMemberRemovedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무에서 제외되었습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskDeadline(TaskDeadlineEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무의 마감일이 하루 남았습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCancelled(TaskCancelledEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무가 취소되었습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무 상태가 변경되었습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectManagerChanged(TaskManagerChangedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무의 담당자가 [" + event.getManagerNm() + "] 으로 변경되었습니다.",
            "04",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCompleted(TaskCompletedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무가 완료 처리되었습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskDeadlineChanged(TaskDeadlineChangedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getTaskNm() + "] 업무의 마감일이 변경되었습니다.",
            "05",
            null,
            event.getRcvrEmpIds()
        );
    }

    // 회의

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingInvited(MeetingInvitedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getMeetingNm() + "] 회의에 초대되었습니다.",
            "06",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingCancelled(MeetingEndedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getMeetingNm() + "] 회의가 종료되었습니다.",
            "06",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingReminder(MeetingReminderEvent event) {
        notificationService.sendAlrm(
            "[" + event.getMeetingNm() + "] 회의 시작까지 10분 남았습니다.",
            "06",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingChanged(MeetingChangedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getMeetingNm() + "] 회의 일정이 [" + event.getMeetingDt() + "] 로 변경되었습니다.",
            "06",
            null,
            event.getEmpIds()
        );
    }

    // 일정

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleCreated(ScheduleCreatedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getSchdNm() + "] 일정이 등록되었습니다.",
            "03",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleCancelled(ScheduleCancelledEvent event) {
        notificationService.sendAlrm(
            "[" + event.getSchdNm() + "] 일정이 취소되었습니다.",
            "03",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleChanged(ScheduleChangedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getSchdNm() + "] 일정이 변경되었습니다.",
            "03",
            null,
            event.getEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleReminder(ScheduleReminderEvent event) {
        notificationService.sendAlrm(
            "[" + event.getSchdNm() + "] 일정 시작 시간이 다가오고 있습니다.",
            "03",
            null,
            event.getEmpIds()
        );
    }

    // 교육

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationEnrolled(EducationEnrolledEvent event) {
        notificationService.sendAlrm(
            "[" + event.getEduNm() + "] 교육 대상자로 등록되었습니다.",
            "08",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationCancelled(EducationCancelledEvent event) {
        notificationService.sendAlrm(
            "[" + event.getEduNm() + "] 교육이 취소되었습니다.",
            "08",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationCompleted(EducationCompletedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getEduNm() + "] 교육을 수료했습니다.",
            "08",
            null,
            List.of(event.getRcvrEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationDeadline(EducationDeadlineEvent event) {
        notificationService.sendAlrm(
            "[" + event.getEduNm() + "] 교육 수강 기한이 임박했습니다.(미수료자)",
            "08",
            null,
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationStarted(EducationStartedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getEduNm() + "] 교육이 시작되었습니다.",
            "08",
            null,
            event.getRcvrEmpIds()
        );
    }

    // 게시판

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        notificationService.sendAlrm(
            "[" + event.getPostTitle() + "] 게시글에 댓글이 등록되었습니다.",
            "07",
            null,
            List.of(event.getRcvrEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeletedByAdmin(PostDeletedByAdminEvent event) {
        notificationService.sendAlrm(
            "[" + event.getPostTitle() + "] 게시글이 관리자에 의해 삭제되었습니다.",
            "07",
            null,
            List.of(event.getRcvrEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoticeCreated(NoticeCreatedEvent event) {
        notificationService.sendAlrm(
            "새로운 공지사항이 등록되었습니다.",
            "01",
            null,
            event.getAllEmpIds()
        );
    }

    // 기타

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(PasswordChangedEvent event) {
        notificationService.sendAlrm(
            "비밀번호가 변경되었습니다.",
            "09",
            null,
            List.of(event.getEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileChanged(ProfileChangedEvent event) {
        notificationService.sendAlrm(
            "개인정보가 변경되었습니다.",
            "09",
            null,
            List.of(event.getEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePermissionChanged(PermissionChangedEvent event) {
        notificationService.sendAlrm(
            "사용 권한이 변경되었습니다.",
            "09",
            null,
            event.getEmpIds()
        );
    }
}
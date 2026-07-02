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
import com.mycrewsoft.domain.mail.event.MailReceivedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingChangedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingEndedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingInvitedEvent;
import com.mycrewsoft.domain.mtng.event.MeetingReminderEvent;
import com.mycrewsoft.domain.notification.enums.NotificationTargetType;
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
            "입사 환영",
            "01",
            "입사를 축하드립니다! 그룹웨어 이용을 시작해 주세요.",
            List.of(event.getEmpId())
        );
    }

    // 전자결재

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalApproved(ApprovalApprovedEvent event) {
        notificationService.sendAlrm(
            "결재 승인",
            "02",
            "신청하신 [" + event.getApprovalNm() + "] 결재가 승인되었습니다.",
            List.of(event.getRcvrEmpId()),
            NotificationTargetType.APPROVAL,
            event.getDrftDocSn(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalRejected(ApprovalRejectedEvent event) {
        notificationService.sendAlrm(
            "결재 반려",
            "02",
            "신청하신 [" + event.getApprovalNm() + "] 결재가 반려되었습니다.",
            List.of(event.getRcvrEmpId()),
            NotificationTargetType.APPROVAL,
            event.getDrftDocSn(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalRequested(ApprovalRequestedEvent event) {
        notificationService.sendAlrm(
            "결재 요청",
            "02",
            event.getApplicantNm() + " 님이 [" + event.getApprovalNm() + "] 결재를 요청했습니다.",
            event.getApproverIds(),
            NotificationTargetType.APPROVAL,
            event.getDrftDocSn(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalDeadline(ApprovalDeadlineEvent event) {
        notificationService.sendAlrm(
            "결재 기한 임박",
            "02",
            "[" + event.getApprovalNm() + "] 결재 처리 기한이 3일 남았습니다.",
            List.of(event.getApproverId()),
            NotificationTargetType.APPROVAL,
            event.getDrftDocSn(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalCancelled(ApprovalCancelledEvent event) {
        notificationService.sendAlrm(
            "결재 요청 취소",
            "02",
            event.getApplicantNm() + " 님이 [" + event.getApprovalNm() + "] 결재 요청을 취소했습니다.",
            event.getApproverIds(),
            NotificationTargetType.APPROVAL,
            event.getDrftDocSn(),
            null
        );
    }

    // 프로젝트

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreated(ProjectCreatedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 등록",
            "04",
            "[" + event.getProjNm() + "] 프로젝트에 등록되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectMembersAdded(ProjectMembersAddedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 참여",
            "04",
            "[" + event.getProjNm() + "] 프로젝트에 참여하게 되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectMemberRemoved(ProjectMemberRemovedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 제외",
            "04",
            "[" + event.getProjNm() + "] 프로젝트에서 제외되었습니다.",
            List.of(event.getEmpId()),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectDeadline(ProjectDeadlineEvent event) {
        notificationService.sendAlrm(
            "프로젝트 마감 임박",
            "04",
            "[" + event.getProjNm() + "] 프로젝트 마감일이 하루 남았습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectClosed(ProjectClosedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 마감",
            "04",
            "[" + event.getProjNm() + "] 프로젝트가 마감되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCancelled(ProjectStoppedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 중지",
            "04",
            "[" + event.getProjNm() + "] 프로젝트가 중지되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectManagerChanged(ProjectManagerChangedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 담당자 변경",
            "04",
            "[" + event.getProjNm() + "] 프로젝트 담당자가 " + event.getManagerNm() + " 님으로 변경되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCompleted(ProjectCompletedEvent event) {
        notificationService.sendAlrm(
            "프로젝트 완료",
            "04",
            "[" + event.getProjNm() + "] 프로젝트가 완료되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.PROJECT,
            event.getProjId(),
            null
        );
    }

    // 업무

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        notificationService.sendAlrm(
            "업무 배정",
            "05",
            "[" + event.getTaskNm() + "] 업무가 배정되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMemberRemoved(TaskMemberRemovedEvent event) {
        notificationService.sendAlrm(
            "업무 제외",
            "05",
            "[" + event.getTaskNm() + "] 업무에서 제외되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskDeadline(TaskDeadlineEvent event) {
        notificationService.sendAlrm(
            "업무 마감 임박",
            "05",
            "[" + event.getTaskNm() + "] 업무 마감일이 하루 남았습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCancelled(TaskCancelledEvent event) {
        notificationService.sendAlrm(
            "업무 취소",
            "05",
            "[" + event.getTaskNm() + "] 업무가 취소되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        notificationService.sendAlrm(
            "업무 상태 변경",
            "05",
            "[" + event.getTaskNm() + "] 업무 상태가 변경되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectManagerChanged(TaskManagerChangedEvent event) {
        notificationService.sendAlrm(
            "업무 담당자 변경",
            "05",
            "[" + event.getTaskNm() + "] 업무 담당자가 " + event.getManagerNm() + " 님으로 변경되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCompleted(TaskCompletedEvent event) {
        notificationService.sendAlrm(
            "업무 완료",
            "05",
            "[" + event.getTaskNm() + "] 업무가 완료되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskDeadlineChanged(TaskDeadlineChangedEvent event) {
        notificationService.sendAlrm(
            "업무 마감일 변경",
            "05",
            "[" + event.getTaskNm() + "] 업무 마감일이 변경되었습니다.",
            event.getRcvrEmpIds(),
            NotificationTargetType.TASK,
            event.getTaskId(),
            event.getProjId()
        );
    }

    // 회의

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingInvited(MeetingInvitedEvent event) {
        notificationService.sendAlrm(
            "회의 초대",
            "06",
            "[" + event.getMeetingNm() + "] 회의에 초대되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.MEETING,
            event.getMeetingId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingEnded(MeetingEndedEvent event) {
        notificationService.sendAlrm(
            "회의 종료",
            "06",
            "[" + event.getMeetingNm() + "] 회의가 종료되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.MEETING,
            event.getMtngId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingReminder(MeetingReminderEvent event) {
        notificationService.sendAlrm(
            "회의 시작 임박",
            "06",
            "[" + event.getMeetingNm() + "] 회의가 10분 후 시작됩니다.",
            event.getEmpIds(),
            NotificationTargetType.MEETING,
            event.getMeetingId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingChanged(MeetingChangedEvent event) {
        notificationService.sendAlrm(
            "회의 일정 변경",
            "06",
            "[" + event.getMeetingNm() + "] 회의 일정이 " + event.getMeetingDt() + " 으로 변경되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.MEETING,
            event.getMeetingId(),
            null
        );
    }

    // 일정

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleCreated(ScheduleCreatedEvent event) {
        notificationService.sendAlrm(
            "일정 등록",
            "03",
            "[" + event.getSchdNm() + "] 일정이 등록되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.SCHEDULE,
            event.getSchdId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleCancelled(ScheduleCancelledEvent event) {
        notificationService.sendAlrm(
            "일정 취소",
            "03",
            "[" + event.getSchdNm() + "] 일정이 취소되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.SCHEDULE,
            event.getSchdId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleChanged(ScheduleChangedEvent event) {
        notificationService.sendAlrm(
            "일정 변경",
            "03",
            "[" + event.getSchdNm() + "] 일정이 변경되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.SCHEDULE,
            event.getSchdId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleReminder(ScheduleReminderEvent event) {
        notificationService.sendAlrm(
            "일정 시작 임박",
            "03",
            "[" + event.getSchdNm() + "] 일정이 곧 시작됩니다.",
            event.getEmpIds(),
            NotificationTargetType.SCHEDULE,
            event.getSchdId(),
            null
        );
    }

    // 교육

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationEnrolled(EducationEnrolledEvent event) {
        notificationService.sendAlrm(
            "교육 등록",
            "08",
            "[" + event.getEduNm() + "] 교육 대상자로 등록되었습니다.",
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationCancelled(EducationCancelledEvent event) {
        notificationService.sendAlrm(
            "교육 취소",
            "08",
            "[" + event.getEduNm() + "] 교육이 취소되었습니다.",
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationCompleted(EducationCompletedEvent event) {
        notificationService.sendAlrm(
            "교육 수료",
            "08",
            "[" + event.getEduNm() + "] 교육을 수료했습니다. 수고하셨습니다!",
            List.of(event.getRcvrEmpId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationDeadline(EducationDeadlineEvent event) {
        notificationService.sendAlrm(
            "교육 수강 기한 임박",
            "08",
            "[" + event.getEduNm() + "] 교육 수강 기한이 임박했습니다. 아직 미수료 상태입니다.",
            event.getRcvrEmpIds()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEducationStarted(EducationStartedEvent event) {
        notificationService.sendAlrm(
            "교육 시작",
            "08",
            "[" + event.getEduNm() + "] 교육이 시작되었습니다.",
            event.getRcvrEmpIds()
        );
    }

    // 게시판

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        notificationService.sendAlrm(
            "댓글 등록",
            "07",
            "내 게시글 [" + event.getPostTitle() + "] 에 댓글이 등록되었습니다.",
            List.of(event.getRcvrEmpId()),
            NotificationTargetType.BOARD,
            event.getBoardId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeletedByAdmin(PostDeletedByAdminEvent event) {
        notificationService.sendAlrm(
            "게시글 삭제",
            "07",
            "[" + event.getPostTitle() + "] 게시글이 관리자에 의해 삭제되었습니다.",
            List.of(event.getRcvrEmpId()),
            NotificationTargetType.BOARD,
            null,
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoticeCreated(NoticeCreatedEvent event) {
        notificationService.sendAlrm(
            "공지사항",
            "01",
            "[" + event.getPostTitle() + "] 새로운 공지사항이 등록되었습니다.",
            event.getAllEmpIds(),
            NotificationTargetType.BOARD,
            event.getBoardId(),
            null
        );
    }

    // 메일

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMailReceived(MailReceivedEvent event) {
        String subject = (event.getLatestSubject() == null || event.getLatestSubject().isBlank())
                ? "(제목 없음)"
                : event.getLatestSubject();
        String content = event.getNewCount() > 1
                ? "[" + subject + "] 외 " + (event.getNewCount() - 1) + "건의 새 메일이 도착했습니다."
                : "[" + subject + "] 새 메일이 도착했습니다.";
        notificationService.sendAlrm("새 메일", "10", content, List.of(event.getEmpId()));
    }

    // 기타

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(PasswordChangedEvent event) {
        notificationService.sendAlrm(
            "비밀번호 변경",
            "09",
            "비밀번호가 변경되었습니다. 본인이 변경한 것이 아니라면 관리자에게 문의하세요.",
            List.of(event.getEmpId()),
            NotificationTargetType.EMPLOYEE,
            event.getEmpId(),
            null
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileChanged(ProfileChangedEvent event) {
        notificationService.sendAlrm(
            "개인정보 변경",
            "09",
            "개인정보가 변경되었습니다.",
            List.of(event.getEmpId()),
            NotificationTargetType.EMPLOYEE,
            event.getEmpId(),
            null
        );
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePermissionChanged(PermissionChangedEvent event) {
        notificationService.sendAlrm(
            "권한 변경",
            "09",
            "사용 권한이 변경되었습니다.",
            event.getEmpIds(),
            NotificationTargetType.EMPLOYEE,
            null,
            null
        );
    }
}
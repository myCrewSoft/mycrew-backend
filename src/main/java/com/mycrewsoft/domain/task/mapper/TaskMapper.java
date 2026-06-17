package com.mycrewsoft.domain.task.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.task.vo.TaskDeadlineVO;
import com.mycrewsoft.domain.task.vo.TaskDetailVO;
import com.mycrewsoft.domain.task.vo.TaskListVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptVO;
import com.mycrewsoft.domain.task.vo.TaskVO;

@Mapper
public interface TaskMapper {

    /** 프로젝트 ID로 업무 목록 조회 (담당자명 JOIN 포함) */
    List<TaskListVO> selectTaskList(@Param("projId") Long projId);

    /** 업무 ID로 업무 상세 조회 (참여자 목록 collection 포함) */
    TaskDetailVO selectTaskDetail(@Param("taskId") Long taskId);

    /** 업무 1건 등록 (등록 후 생성된 taskId를 vo에 자동 반영) */
    int insertTask(TaskVO vo);

    /** 업무 참여자 1명 등록 (empIdList 순회 시 반복 호출) */
    int insertTaskPtcpt(TaskPtcptVO vo);

    /** 업무 정보 수정 (null 필드는 UPDATE 제외) */
    void updateTask(@Param("taskId") Long taskId, @Param("task") TaskVO task);

    /** 업무 담당자 변경 (단일 컬럼 업데이트) */
    int updateTaskManager(@Param("taskId") Long taskId,
                           @Param("taskMngrId") Long taskMngrId,
                           @Param("lastMdfrId") Long lastMdfrId);

    /** 업무 참여자 전체 삭제 (수정 시 DELETE → INSERT 패턴에 사용) */
    int deleteTaskPtcpts(@Param("taskId") Long taskId);

    /** 업무 참여자 1명 삭제 (taskId + empId로 특정) */
    int deleteTaskPtcpt(@Param("taskId") Long taskId,
                         @Param("empId") Long empId);

    /** 업무 논리 삭제 (DEL_YN = Y, 삭제자 ID · 삭제일시 기록) */
    int deleteTask(@Param("taskId") Long taskId,
                    @Param("deltrMbrId") Long deltrMbrId);

    /** 마감 임박 업무 조회 */
    List<TaskDeadlineVO> selectTasksDueTomorrow();
    
    /** 위젯용 */
    List<TaskListVO> selectTaskListForWidget(@Param("empId") Long empId, @Param("limit") int limit);
}
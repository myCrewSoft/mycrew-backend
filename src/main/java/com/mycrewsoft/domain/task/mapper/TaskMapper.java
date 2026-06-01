package com.mycrewsoft.domain.task.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.task.vo.TaskVO;

@Mapper
public interface TaskMapper {

    TaskVO selectTaskAuthContextByTaskId(Long taskId);
}

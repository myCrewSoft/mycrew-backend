package com.mycrewsoft.domain.approval.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;

@Mapper
public interface ApprovalAiMapper {

    List<ApprovalAiApproverCandidateDTO> selectApproverCandidates(
            @Param("empId") Long empId,
            @Param("deptCd") String deptCd);
}

package com.mycrewsoft.domain.department.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.department.dto.response.AdminDepartmentMemberResponseDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.vo.DepartmentVO;

@Mapper
public interface AdminDepartmentMapper {

    void insertDepartment(DepartmentVO department);

    String selectNextDepartmentCode();

    AdminDepartmentResponseDTO selectDepartmentByCode(@Param("deptCd") String deptCd);

    List<AdminDepartmentResponseDTO> selectDepartments();

    int countActiveDepartmentByCode(@Param("deptCd") String deptCd);

    int countChildDepartments(@Param("deptCd") String deptCd);

    int countDescendantDepartment(
            @Param("deptCd") String deptCd,
            @Param("candidateParentDeptCd") String candidateParentDeptCd);

    int updateDepartment(DepartmentVO department);

    int disableDepartment(
            @Param("deptCd") String deptCd,
            @Param("lastMdfrId") Long lastMdfrId);

    List<Long> selectEnabledEmpIdsByDeptCd(@Param("deptCd") String deptCd);

    List<AdminDepartmentMemberResponseDTO> selectDepartmentMembers(@Param("deptCd") String deptCd);

    int countEnabledEmployeesByIds(@Param("empIds") List<Long> empIds);

    int countEmployeesByDeptAndIds(
            @Param("deptCd") String deptCd,
            @Param("empIds") List<Long> empIds);

    int updateDepartmentForEmployees(
            @Param("targetDeptCd") String targetDeptCd,
            @Param("empIds") List<Long> empIds);

    int updateDepartmentForAllEmployees(
            @Param("sourceDeptCd") String sourceDeptCd,
            @Param("targetDeptCd") String targetDeptCd);
}

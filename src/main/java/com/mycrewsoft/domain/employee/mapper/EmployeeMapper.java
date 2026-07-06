package com.mycrewsoft.domain.employee.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.dto.response.EmployeeMyPageResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;

@Mapper
public interface EmployeeMapper {
	/**
	 * 사번으로 사원의 부서코드를 조회하는 메서드
	 * @param empId
	 * @return deptCd
	 */
	String selectEmpDeptCodeByEmpId(Long empId);
	
	/**
	 * 사번으로 사원이 존재하는지 여부를 반환하는 메서드
	 * @param empId
	 * @return boolean
	 */
	boolean existsByEmpId(Long empId);
	
	/**
	 * 사번으로 사원의 간략한 정보를 조회하는 메서드
	 * @param empId
	 * @return EmployeeProfileDTO
	 */
	EmployeeProfileDTO selectEmployeeProfileByEmpId(Long empId);
	
	/**
	 * 사번으로 사원의 마이페이지 정보를 조회하는 메서드
	 * @param empId
	 * @return EmployeeMyPageResponseDTO
	 */
	EmployeeMyPageResponseDTO selectEmployeeMyPageByEmpId(Long empId);
	
	/**
	 * 사번으로 사원의 비밀번호를 조회하는 메서드
	 * @param empId
	 * @return String
	 */
	String selectPasswordByEmpId(Long empId);
	
	/**
	 * 사원의 비밀번호를 업데이트 하는 메서드
	 * @param empId
	 * @param newPasswordHash
	 */
	int updatePasswordByEmpId(@Param("empId") Long empId, @Param("newPassword") String newPasswordHash);
	
	int updateSignatureByEmpId(@Param("empId") Long empId, @Param("mbrStampFileId") Long mbrStampFileId);
	
	int updateProfileImageByEmpId(@Param("empId") Long empId, @Param("prflImgFileId") Long prflImgFileId);
	
	int updateJobDutyByEmpId(@Param("empId") Long empId, @Param("jobDutyCn") String jobDutyCn);

	int updateProfileInfoByEmpId(
			@Param("empId") Long empId,
			@Param("empNm") String empNm,
			@Param("mblTelno") String mblTelno,
			@Param("zip") String zip,
			@Param("addr") String addr);
}

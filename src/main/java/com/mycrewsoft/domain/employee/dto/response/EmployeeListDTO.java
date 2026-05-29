package com.mycrewsoft.domain.employee.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Employee list response DTO for admin employee search.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Employee list response DTO")
public class EmployeeListDTO {

    private Long empId;
    private String deptCd;
    private String jobPstnCd;
    private String jobGrdCd;
    private String empStatCd;
    private String jobDutyCn;
    private String empNm;
    private String genderCd;
    private String mblTelno;
    private String zip;
    private String addr;
    private Long prflImgFileId;
    private String execYn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate retcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime frstRegDt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMdfcnDt;

    private String enabled;
    private List<RoleAssignmentVO> roleAssignmentList;
}

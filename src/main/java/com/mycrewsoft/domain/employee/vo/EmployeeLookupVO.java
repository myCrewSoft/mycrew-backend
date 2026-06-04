package com.mycrewsoft.domain.employee.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLookupVO {

    private Long empId;
    private String empNm;
    private String deptNm;
    private String jobGrdNm;   // 직급명
    private String jobPstnNm;   // 직책명PRFL_IMG_FILE_ID
    private String profileImageUrl;
}
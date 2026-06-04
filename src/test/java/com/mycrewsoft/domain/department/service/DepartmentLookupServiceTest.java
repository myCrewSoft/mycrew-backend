package com.mycrewsoft.domain.department.service;

import com.mycrewsoft.domain.department.dto.response.DepartmentLookupResponse;
import com.mycrewsoft.domain.department.mapper.DepartmentLookupDtoMapper;
import com.mycrewsoft.domain.department.mapper.DepartmentLookupMapper;
import com.mycrewsoft.domain.department.vo.DepartmentLookupVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentLookupServiceTest {

    @InjectMocks
    private DepartmentLookupServiceImpl departmentLookupService;

    @Mock
    private DepartmentLookupMapper departmentLookupMapper;

    @Mock
    private DepartmentLookupDtoMapper departmentLookupDtoMapper;

    @Test
    @DisplayName("부서 목록 조회 시 전체 부서를 반환한다")
    void lookupDepartments_returnsAllDepartments() {
        List<DepartmentLookupVO> voList = List.of(
                DepartmentLookupVO.builder()
                        .deptCd("D001")
                        .deptNm("개발팀")
                        .build(),
                DepartmentLookupVO.builder()
                        .deptCd("D002")
                        .deptNm("인사팀")
                        .build()
        );

        List<DepartmentLookupResponse> responseList = List.of(
                DepartmentLookupResponse.builder()
                        .deptCd("D001")
                        .deptNm("개발팀")
                        .build(),
                DepartmentLookupResponse.builder()
                        .deptCd("D002")
                        .deptNm("인사팀")
                        .build()
        );

        given(departmentLookupMapper.selectDepartmentsForLookup()).willReturn(voList);
        given(departmentLookupDtoMapper.toResponseList(voList)).willReturn(responseList);

        List<DepartmentLookupResponse> result = departmentLookupService.lookupDepartments();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDeptCd()).isEqualTo("D001");
        assertThat(result.get(0).getDeptNm()).isEqualTo("개발팀");
        verify(departmentLookupMapper).selectDepartmentsForLookup();
    }

    @Test
    @DisplayName("부서가 없으면 빈 리스트를 반환한다")
    void lookupDepartments_noDepartments_returnsEmptyList() {
        given(departmentLookupMapper.selectDepartmentsForLookup()).willReturn(List.of());
        given(departmentLookupDtoMapper.toResponseList(List.of())).willReturn(List.of());

        List<DepartmentLookupResponse> result = departmentLookupService.lookupDepartments();

        assertThat(result).isEmpty();
        verify(departmentLookupMapper).selectDepartmentsForLookup();
    }
}
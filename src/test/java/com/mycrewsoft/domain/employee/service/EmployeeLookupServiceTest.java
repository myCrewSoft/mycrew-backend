package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.EmployeeLookupRequest;
import com.mycrewsoft.domain.employee.dto.response.EmployeeLookupResponse;
import com.mycrewsoft.domain.employee.mapper.EmployeeLookupDtoMapper;
import com.mycrewsoft.domain.employee.mapper.EmployeeLookupMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeLookupVO;

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
class EmployeeLookupServiceTest {

    @InjectMocks
    private EmployeeLookupServiceImpl employeeLookupService;

    @Mock
    private EmployeeLookupMapper employeeLookupMapper;

    @Mock
    private EmployeeLookupDtoMapper employeeLookupDtoMapper;

    @Test
    @DisplayName("keyword로 검색하면 매핑된 결과를 반환한다")
    void lookupEmployees_withKeyword_returnsResult() {
        EmployeeLookupRequest request = EmployeeLookupRequest.builder()
                .keyword("김철수")
                .build();

        List<EmployeeLookupVO> voList = List.of(
                EmployeeLookupVO.builder()
                        .empId(1L)
                        .empNm("김철수")
                        .deptNm("개발팀")
                        .jobGrdNm("대리")
                        .jobPstnNm(null)
                        .profileImageUrl(null)
                        .build()
        );

        List<EmployeeLookupResponse> responseList = List.of(
                EmployeeLookupResponse.builder()
                        .id(1L)
                        .name("김철수")
                        .department("개발팀")
                        .position("대리")
                        .profileImageUrl(null)
                        .build()
        );

        given(employeeLookupMapper.selectEmployeesForLookup(request)).willReturn(voList);
        given(employeeLookupDtoMapper.toResponseList(voList)).willReturn(responseList);

        List<EmployeeLookupResponse> result = employeeLookupService.lookupEmployees(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("김철수");
        assertThat(result.get(0).getPosition()).isEqualTo("대리");
    }

    @Test
    @DisplayName("keyword 없이 전체 조회하면 전체 결과를 반환한다")
    void lookupEmployees_withoutKeyword_returnsAll() {
        EmployeeLookupRequest request = EmployeeLookupRequest.builder()
                .build();

        List<EmployeeLookupVO> voList = List.of(
                EmployeeLookupVO.builder()
                        .empId(1L)
                        .empNm("김철수")
                        .deptNm("개발팀")
                        .jobGrdNm("대리")
                        .jobPstnNm(null)
                        .profileImageUrl(null)
                        .build(),
                EmployeeLookupVO.builder()
                        .empId(2L)
                        .empNm("이영희")
                        .deptNm("인사팀")
                        .jobGrdNm("과장")
                        .jobPstnNm("팀장")
                        .profileImageUrl("/files/profile.jpg")
                        .build()
        );

        List<EmployeeLookupResponse> responseList = List.of(
                EmployeeLookupResponse.builder()
                        .id(1L).name("김철수").department("개발팀").position("대리").build(),
                EmployeeLookupResponse.builder()
                        .id(2L).name("이영희").department("인사팀").position("팀장").build()
        );

        given(employeeLookupMapper.selectEmployeesForLookup(request)).willReturn(voList);
        given(employeeLookupDtoMapper.toResponseList(voList)).willReturn(responseList);

        List<EmployeeLookupResponse> result = employeeLookupService.lookupEmployees(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getPosition()).isEqualTo("팀장");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 리스트를 반환한다")
    void lookupEmployees_noResult_returnsEmptyList() {
        EmployeeLookupRequest request = EmployeeLookupRequest.builder()
                .keyword("존재하지않는이름")
                .build();

        given(employeeLookupMapper.selectEmployeesForLookup(request)).willReturn(List.of());
        given(employeeLookupDtoMapper.toResponseList(List.of())).willReturn(List.of());

        List<EmployeeLookupResponse> result = employeeLookupService.lookupEmployees(request);

        assertThat(result).isEmpty();
        verify(employeeLookupMapper).selectEmployeesForLookup(request);
    }
}
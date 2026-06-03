package com.mycrewsoft.domain.department.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.department.dto.response.DepartmentLookupResponse;
import com.mycrewsoft.domain.department.service.DepartmentLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "부서 조회", description = "공통 부서 검색 컴포넌트용 API")
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentLookupController {

    private final DepartmentLookupService departmentLookupService;

    @Operation(summary = "부서 목록 조회", description = "사원 검색 부서 필터용 전체 부서 목록 조회")
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<DepartmentLookupResponse>>> lookupDepartments() {
        List<DepartmentLookupResponse> result = departmentLookupService.lookupDepartments();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
package com.mycrewsoft.domain.empstat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.domain.empstat.mapper.EmpStatMapper;

@ExtendWith(MockitoExtension.class)
class EmpStatSyncServiceTest {

    @Mock
    private EmpStatMapper empStatMapper;

    @Test
    void syncEmpStatCodesMergesOnlyEmployeeLifecycleStatusCodes() {
        EmpStatSyncService empStatSyncService = new EmpStatSyncService(empStatMapper);

        when(empStatMapper.mergeEmpStats(argThat(this::containsOnlyLifecycleCodes)))
                .thenReturn(5);

        int insertedCount = empStatSyncService.syncEmpStatCodes();

        assertThat(insertedCount).isEqualTo(5);
        verify(empStatMapper).mergeEmpStats(argThat(this::containsOnlyLifecycleCodes));
    }

    private boolean containsOnlyLifecycleCodes(List<EmpStatSeed> empStats) {
        if (empStats == null) {
            return false;
        }

        List<String> codes = empStats.stream()
                .map(EmpStatSeed::code)
                .toList();

        return codes.size() == 5
                && codes.contains(EmpStatCode.EMP_INITIAL.getCode())
                && codes.contains(EmpStatCode.EMP_ACTIVE.getCode())
                && codes.contains(EmpStatCode.EMP_INACTIVE.getCode())
                && codes.contains(EmpStatCode.EMP_RETIRED.getCode())
                && codes.contains(EmpStatCode.EMP_VACATION.getCode())
                && !codes.contains("EMP_LOGIN")
                && !codes.contains("EMP_LOGOUT");
    }
}

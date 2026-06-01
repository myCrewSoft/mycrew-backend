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
    void syncEmpStatCodesMergesAllEmpStatCodeValues() {
        EmpStatSyncService empStatSyncService = new EmpStatSyncService(empStatMapper);

        when(empStatMapper.mergeEmpStats(argThat(this::containsLoginAndLogoutCodes)))
                .thenReturn(2);

        int insertedCount = empStatSyncService.syncEmpStatCodes();

        assertThat(insertedCount).isEqualTo(2);
        verify(empStatMapper).mergeEmpStats(argThat(this::containsLoginAndLogoutCodes));
    }

    private boolean containsLoginAndLogoutCodes(List<EmpStatSeed> empStats) {
        if (empStats == null) {
            return false;
        }

        List<String> codes = empStats.stream()
                .map(EmpStatSeed::code)
                .toList();

        return codes.contains(EmpStatCode.EMP_LOGIN.getCode())
                && codes.contains(EmpStatCode.EMP_LOGOUT.getCode());
    }
}

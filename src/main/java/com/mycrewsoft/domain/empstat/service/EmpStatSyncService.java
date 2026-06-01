package com.mycrewsoft.domain.empstat.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.domain.empstat.mapper.EmpStatMapper;

import lombok.RequiredArgsConstructor;

/**
 * EmpStatCode enum을 기준으로 TB_EMP_STAT 코드 데이터를 동기화하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class EmpStatSyncService {

    private final EmpStatMapper empStatMapper;

    @Transactional
    public int syncEmpStatCodes() {
        List<EmpStatSeed> empStats = Arrays.stream(EmpStatCode.values())
                .map(EmpStatSeed::from)
                .toList();

        if (empStats.isEmpty()) {
            return 0;
        }

        return empStatMapper.mergeEmpStats(empStats);
    }
}

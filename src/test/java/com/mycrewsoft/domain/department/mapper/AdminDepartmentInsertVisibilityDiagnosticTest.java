package com.mycrewsoft.domain.department.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.vo.DepartmentVO;

@SpringBootTest
@Disabled("진단용 테스트: TB_DEPARTMENT.DEPT_CD CHAR 패딩 원인 확인 후 일반 테스트에서는 제외")
class AdminDepartmentInsertVisibilityDiagnosticTest {

    @Autowired
    private AdminDepartmentMapper departmentMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void diagnoseInsertThenSelectVisibility(TestInfo testInfo) {
        printDatabaseObjects();

        String deptCd = departmentMapper.selectNextDepartmentCode();
        String deptNm = "DIAG_" + System.currentTimeMillis();
        System.out.println("[DIAG] generatedDeptCd=" + deptCd);
        System.out.println("[DIAG] beforeInsertExactCount=" + countExact(deptCd));

        DepartmentVO department = new DepartmentVO();
        department.setDeptCd(deptCd);
        department.setPrntDeptCd(null);
        department.setDeptNm(deptNm);
        department.setUseYn("Y");
        department.setFrstRgtrId(1234L);
        department.setFrstRegDt(LocalDateTime.now());
        department.setLastMdfrId(1234L);
        department.setLastMdfcnDt(LocalDateTime.now());

        departmentMapper.insertDepartment(department);

        int exactCount = countExact(deptCd);
        int trimCount = countTrim(deptCd);
        int useYnCount = countExactWithUseYn(deptCd);
        int trimUseYnCount = countTrimWithUseYn(deptCd);
        List<Map<String, Object>> byNameRows = selectByName(deptNm);
        AdminDepartmentResponseDTO mapperResult = departmentMapper.selectDepartmentByCode(deptCd);

        System.out.println("[DIAG] afterInsertExactCount=" + exactCount);
        System.out.println("[DIAG] afterInsertTrimCount=" + trimCount);
        System.out.println("[DIAG] afterInsertUseYnCount=" + useYnCount);
        System.out.println("[DIAG] afterInsertTrimUseYnCount=" + trimUseYnCount);
        System.out.println("[DIAG] mapperResult=" + formatDepartment(mapperResult));
        System.out.println("[DIAG] rowsByDeptNm=" + byNameRows);

        assertThat(trimCount)
                .as("Inserted department must be visible by trimmed DEPT_CD in the same transaction. rowsByDeptNm=%s", byNameRows)
                .isEqualTo(1);
        assertThat(trimUseYnCount)
                .as("Inserted department must pass trimmed DEPT_CD and USE_YN filter. rowsByDeptNm=%s", byNameRows)
                .isEqualTo(1);
        assertThat(mapperResult)
                .as("AdminDepartmentMapper.selectDepartmentByCode must return the inserted row. rowsByDeptNm=%s", byNameRows)
                .isNotNull();
    }

    private void printDatabaseObjects() {
        List<Map<String, Object>> triggers = jdbcTemplate.queryForList("""
                SELECT TRIGGER_NAME, STATUS, TRIGGERING_EVENT, TRIGGER_TYPE
                FROM USER_TRIGGERS
                WHERE TABLE_NAME = 'TB_DEPARTMENT'
                ORDER BY TRIGGER_NAME
                """);
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("""
                SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE, DATA_DEFAULT
                FROM USER_TAB_COLUMNS
                WHERE TABLE_NAME = 'TB_DEPARTMENT'
                  AND COLUMN_NAME IN ('DEPT_CD', 'USE_YN', 'DEPT_NM')
                ORDER BY COLUMN_ID
                """);
        List<Map<String, Object>> sequences = jdbcTemplate.queryForList("""
                SELECT SEQUENCE_NAME, LAST_NUMBER, CACHE_SIZE, INCREMENT_BY
                FROM USER_SEQUENCES
                WHERE SEQUENCE_NAME = 'SEQ_DEPARTMENT'
                """);

        System.out.println("[DIAG] triggers=" + triggers);
        System.out.println("[DIAG] columns=" + columns);
        System.out.println("[DIAG] sequence=" + sequences);
    }

    private int countExact(String deptCd) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM TB_DEPARTMENT WHERE DEPT_CD = ?",
                Integer.class,
                deptCd);
    }

    private int countTrim(String deptCd) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM TB_DEPARTMENT WHERE TRIM(DEPT_CD) = ?",
                Integer.class,
                deptCd);
    }

    private int countExactWithUseYn(String deptCd) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM TB_DEPARTMENT WHERE DEPT_CD = ? AND NVL(USE_YN, 'Y') = 'Y'",
                Integer.class,
                deptCd);
    }

    private int countTrimWithUseYn(String deptCd) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM TB_DEPARTMENT WHERE TRIM(DEPT_CD) = ? AND NVL(USE_YN, 'Y') = 'Y'",
                Integer.class,
                deptCd);
    }

    private List<Map<String, Object>> selectByName(String deptNm) {
        return jdbcTemplate.queryForList("""
                SELECT DEPT_CD, DEPT_NM, USE_YN, PRNT_DEPT_CD
                FROM TB_DEPARTMENT
                WHERE DEPT_NM = ?
                ORDER BY DEPT_CD
                """, deptNm);
    }

    private String formatDepartment(AdminDepartmentResponseDTO department) {
        if (department == null) {
            return "null";
        }
        return "{deptCd=" + department.getDeptCd()
                + ", deptNm=" + department.getDeptNm()
                + ", useYn=" + department.getUseYn()
                + ", memberCount=" + department.getMemberCount()
                + "}";
    }
}

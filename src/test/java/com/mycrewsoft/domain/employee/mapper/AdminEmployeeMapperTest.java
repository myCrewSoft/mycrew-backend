package com.mycrewsoft.domain.employee.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mycrewsoft.domain.employee.dto.request.EmployeeSearchDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;

@SpringBootTest
class AdminEmployeeMapperTest {

    private static final long EMP_ID_1 = 880000001L;
    private static final long EMP_ID_2 = 880000002L;
    private static final long EMP_ID_3 = 880000003L;

    private static final String DEPT_HR = "TEST_HR";
    private static final String DEPT_ACC = "TEST_ACC";
    private static final String JOB_GRD = "TG001";
    private static final String JOB_PSTN = "TP001";

    @Autowired
    private AdminEmployeeMapper adminEmployeeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        try {
            deleteEmployees();
            seedReferenceRows();
            insertEmployee(EMP_ID_1, "Alpha HR", DEPT_HR, "EMP_ACTIVE", "Y", "Y");
            insertEmployee(EMP_ID_2, "Bravo HR", DEPT_HR, "EMP_ACTIVE", "N", "Y");
            insertEmployee(EMP_ID_3, "Charlie ACC", DEPT_ACC, "EMP_INACTIVE", "N", "N");
        } catch (DataAccessException e) {
            Assumptions.abort("Oracle test schema is not ready for AdminEmployeeMapperTest: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanUp() {
        try {
            deleteEmployees();
            jdbcTemplate.update("DELETE FROM TB_DEPARTMENT WHERE DEPT_CD IN (?, ?)", DEPT_HR, DEPT_ACC);
            jdbcTemplate.update("DELETE FROM TB_JOB_GRADE WHERE JOB_GRD_CD = ?", JOB_GRD);
            jdbcTemplate.update("DELETE FROM TB_JOB_POSITION WHERE JOB_PSTN_CD = ?", JOB_PSTN);
        } catch (DataAccessException ignored) {
            // Cleanup is best-effort for local integration tests.
        }
    }

    @Test
    void countEmployeesAppliesSearchConditions() {
        EmployeeSearchDTO condition = new EmployeeSearchDTO();
        condition.setDeptCd(DEPT_HR);
        condition.setEmpStatCd("EMP_ACTIVE");
        condition.setEnabled("Y");

        long count = adminEmployeeMapper.countEmployees(condition);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void selectEmployeesAppliesKeywordFilterAndPagination() {
        EmployeeSearchDTO condition = new EmployeeSearchDTO();
        condition.setKeyword("HR");
        condition.setDeptCd(DEPT_HR);
        condition.setSize(1);

        List<EmployeeListDTO> firstPage = adminEmployeeMapper.selectEmployees(condition, 0, 1);
        List<EmployeeListDTO> secondPage = adminEmployeeMapper.selectEmployees(condition, 1, 1);

        assertThat(firstPage).hasSize(1);
        assertThat(secondPage).hasSize(1);
        assertThat(firstPage.get(0).getEmpId()).isEqualTo(EMP_ID_2);
        assertThat(secondPage.get(0).getEmpId()).isEqualTo(EMP_ID_1);
        assertThat(firstPage.get(0).getDeptCd().trim()).isEqualTo(DEPT_HR);
        assertThat(firstPage.get(0).getJobGrdCd().trim()).isEqualTo(JOB_GRD);
        assertThat(firstPage.get(0).getJobPstnCd().trim()).isEqualTo(JOB_PSTN);
    }

    @Test
    void selectEmployeesFiltersByExecutiveAndEnabledFlags() {
        EmployeeSearchDTO condition = new EmployeeSearchDTO();
        condition.setExecYn("N");
        condition.setEnabled("N");

        List<EmployeeListDTO> employees = adminEmployeeMapper.selectEmployees(condition, 0, 10);

        assertThat(employees)
                .extracting(EmployeeListDTO::getEmpId)
                .containsExactly(EMP_ID_3);
    }

    private void seedReferenceRows() {
        jdbcTemplate.update("""
                MERGE INTO TB_EMP_STAT S
                USING (
                    SELECT 'EMP_ACTIVE' AS EMP_STAT_CD, 'Active' AS EMP_STAT_NM, 'Active employee' AS EMP_STAT_EXPLN FROM DUAL
                    UNION ALL
                    SELECT 'EMP_INACTIVE', 'Inactive', 'Inactive employee' FROM DUAL
                ) SRC
                   ON (S.EMP_STAT_CD = SRC.EMP_STAT_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (EMP_STAT_CD, EMP_STAT_NM, EMP_STAT_EXPLN)
                   VALUES (SRC.EMP_STAT_CD, SRC.EMP_STAT_NM, SRC.EMP_STAT_EXPLN)
                """);

        jdbcTemplate.update("""
                MERGE INTO TB_DEPARTMENT D
                USING (
                    SELECT ? AS DEPT_CD, 'Test HR' AS DEPT_NM FROM DUAL
                    UNION ALL
                    SELECT ? AS DEPT_CD, 'Test Accounting' AS DEPT_NM FROM DUAL
                ) SRC
                   ON (D.DEPT_CD = SRC.DEPT_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (DEPT_CD, PRNT_DEPT_CD, DEPT_NM, USE_YN, FRST_REG_DT, FRST_RGTR_ID, LAST_MDFCN_DT, LAST_MDFR_ID)
                   VALUES (SRC.DEPT_CD, NULL, SRC.DEPT_NM, 'Y', SYSDATE, 0, NULL, NULL)
                """, DEPT_HR, DEPT_ACC);

        jdbcTemplate.update("""
                MERGE INTO TB_JOB_GRADE G
                USING (SELECT ? AS JOB_GRD_CD, 'Test Grade' AS JOB_GRD_NM FROM DUAL) SRC
                   ON (G.JOB_GRD_CD = SRC.JOB_GRD_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (JOB_GRD_CD, JOB_GRD_NM, USE_YN, FRST_RGTR_ID, FRST_REG_DT, LAST_MDFR_ID, LAST_MDFCN_DT)
                   VALUES (SRC.JOB_GRD_CD, SRC.JOB_GRD_NM, 'Y', 0, SYSDATE, NULL, NULL)
                """, JOB_GRD);

        jdbcTemplate.update("""
                MERGE INTO TB_JOB_POSITION P
                USING (SELECT ? AS JOB_PSTN_CD, 'Test Position' AS JOB_PSTN_NM FROM DUAL) SRC
                   ON (P.JOB_PSTN_CD = SRC.JOB_PSTN_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (JOB_PSTN_CD, JOB_PSTN_NM, USE_YN, FRST_RGTR_ID, FRST_REG_DT, LAST_MDFR_ID, LAST_MDFCN_DT)
                   VALUES (SRC.JOB_PSTN_CD, SRC.JOB_PSTN_NM, 'Y', 0, SYSDATE, NULL, NULL)
                """, JOB_PSTN);
    }

    private void insertEmployee(
            long empId,
            String empNm,
            String deptCd,
            String empStatCd,
            String execYn,
            String enabled) {
        jdbcTemplate.update("""
                INSERT INTO TB_EMPLOYEE (
                    EMP_ID,
                    EMP_STAT_CD,
                    JOB_GRD_CD,
                    JOB_PSTN_CD,
                    DEPT_CD,
                    JOB_DUTY_CN,
                    PSWD,
                    EMP_NM,
                    RRNO,
                    GENDER_CD,
                    MBL_TELNO,
                    ZIP,
                    ADDR,
                    EXEC_YN,
                    ENTCO_YMD,
                    FRST_REG_DT,
                    ENABLED
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    'Test duty',
                    '{noop}test',
                    ?,
                    ?,
                    '1',
                    ?,
                    '12345',
                    'Test address',
                    ?,
                    SYSDATE,
                    SYSDATE + ?,
                    ?
                )
                """,
                empId,
                empStatCd,
                JOB_GRD,
                JOB_PSTN,
                deptCd,
                empNm,
                String.valueOf(empId).substring(0, 6) + "1234567",
                "010-" + String.valueOf(empId).substring(3, 7) + "-" + String.valueOf(empId).substring(5, 9),
                execYn,
                empId - EMP_ID_1,
                enabled);
    }

    private void deleteEmployees() {
        jdbcTemplate.update("DELETE FROM TB_ROLE_ASSIGNMENT WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
        jdbcTemplate.update("DELETE FROM TB_AUTH_VERSION WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
        jdbcTemplate.update("DELETE FROM TB_EMPLOYEE WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
    }
}

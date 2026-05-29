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
import com.mycrewsoft.domain.employee.dto.response.EmployeeDetailDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

@SpringBootTest
class AdminEmployeeMapperTest {

    private static final long EMP_ID_1 = 880000001L;
    private static final long EMP_ID_2 = 880000002L;
    private static final long EMP_ID_3 = 880000003L;

    private static final String DEPT_HR = "TEST_HR";
    private static final String DEPT_ACC = "TEST_ACC";
    private static final String JOB_GRD = "TG001";
    private static final String JOB_PSTN = "TP001";
    private static final long ROLE_ID_ADMIN = 889900001L;
    private static final long ROLE_ID_MANAGER = 889900002L;

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
            insertMailAccount(EMP_ID_1, "google", "alpha.hr@example.com");
            insertRole(ROLE_ID_ADMIN, "TEST_ADMIN", "Test Admin");
            insertRole(ROLE_ID_MANAGER, "TEST_MANAGER", "Test Manager");
            insertRoleAssignment(889910001L, ROLE_ID_ADMIN, EMP_ID_1, "GLOBAL", "ALL");
            insertRoleAssignment(889910002L, ROLE_ID_MANAGER, EMP_ID_1, "DEPARTMENT", DEPT_HR);
        } catch (DataAccessException e) {
            Assumptions.abort("Oracle test schema is not ready for AdminEmployeeMapperTest: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanUp() {
        try {
            deleteEmployees();
            jdbcTemplate.update("DELETE FROM TB_ROLE WHERE ROLE_ID IN (?, ?)", ROLE_ID_ADMIN, ROLE_ID_MANAGER);
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

    @Test
    void selectEmployeeDetailByIdMapsReferenceObjectsMailAccountAndRoles() {
        EmployeeDetailDTO employee = adminEmployeeMapper.selectEmployeeDetailById(EMP_ID_1);

        assertThat(employee).isNotNull();
        assertThat(employee.getEmpId()).isEqualTo(EMP_ID_1);
        assertThat(employee.getEmpNm()).isEqualTo("Alpha HR");
        assertThat(employee.getDepartment().getDeptCd().trim()).isEqualTo(DEPT_HR);
        assertThat(employee.getDepartment().getDeptNm()).isEqualTo("Test HR");
        assertThat(employee.getJobGrade().getJobGrdCd().trim()).isEqualTo(JOB_GRD);
        assertThat(employee.getJobGrade().getJobGrdNm()).isEqualTo("Test Grade");
        assertThat(employee.getJobPosition().getJobPstnCd().trim()).isEqualTo(JOB_PSTN);
        assertThat(employee.getJobPosition().getJobPstnNm()).isEqualTo("Test Position");
        assertThat(employee.getEmpStat().getEmpStatCd()).isEqualTo("EMP_ACTIVE");
        assertThat(employee.getEmpStat().getEmpStatNm()).isEqualTo("Active");

        assertThat(employee.getMailAccountList()).hasSize(1);
        assertThat(employee.getMailAccountList().get(0).getEmpId()).isEqualTo(EMP_ID_1);
        assertThat(employee.getMailAccountList().get(0).getProviderCd()).isEqualTo("google");
        assertThat(employee.getMailAccountList().get(0).getEmailAddr()).isEqualTo("alpha.hr@example.com");

        assertThat(employee.getRoleAssignmentList()).hasSize(2);
        assertThat(employee.getRoleAssignmentList())
                .extracting(roleAssignment -> roleAssignment.getRole().getRoleCd())
                .containsExactlyInAnyOrder("TEST_ADMIN", "TEST_MANAGER");
        assertThat(employee.getRoleAssignmentList())
                .extracting(RoleAssignmentVO::getScopeTypeCd)
                .containsExactlyInAnyOrder("GLOBAL", "DEPARTMENT");
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

    private void insertMailAccount(long empId, String providerCd, String emailAddr) {
        jdbcTemplate.update("""
                INSERT INTO TB_MAIL_ACCOUNT (
                    EMP_ID,
                    PROVIDER_CD,
                    EMAIL_ADDR,
                    GOOGLE_SUB_ID,
                    ACCESS_TOKEN,
                    REFRESH_TOKEN,
                    TOKEN_EXPR_DT,
                    SYNC_LAST_DT,
                    USE_YN,
                    FRST_REG_DT,
                    LAST_MDFCN_DT
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    'access-token',
                    'refresh-token',
                    SYSDATE + 1,
                    SYSDATE,
                    'Y',
                    SYSDATE,
                    SYSDATE
                )
                """,
                empId,
                providerCd,
                emailAddr,
                "google-sub-" + empId);
    }

    private void insertRole(long roleId, String roleCd, String roleNm) {
        jdbcTemplate.update("""
                INSERT INTO TB_ROLE (
                    ROLE_ID,
                    ROLE_CD,
                    ROLE_NM,
                    ROLE_EXPLN,
                    FRST_RGTR_ID,
                    FRST_REG_DT,
                    LAST_MDFR_ID,
                    LAST_MDFCN_DT
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    'Role for mapper integration test',
                    0,
                    SYSDATE,
                    0,
                    SYSDATE
                )
                """,
                roleId,
                roleCd,
                roleNm);
    }

    private void insertRoleAssignment(long roleAssignId, long roleId, long empId, String scopeTypeCd, String scopeId) {
        jdbcTemplate.update("""
                INSERT INTO TB_ROLE_ASSIGNMENT (
                    ROLE_ASSIGN_ID,
                    ROLE_ID,
                    EMP_ID,
                    SCOPE_TYPE_CD,
                    SCOPE_ID,
                    ENABLED
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    'Y'
                )
                """,
                roleAssignId,
                roleId,
                empId,
                scopeTypeCd,
                scopeId);
    }

    private void deleteEmployees() {
        jdbcTemplate.update("DELETE FROM TB_ROLE_ASSIGNMENT WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
        jdbcTemplate.update("DELETE FROM TB_MAIL_ACCOUNT WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
        jdbcTemplate.update("DELETE FROM TB_AUTH_VERSION WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
        jdbcTemplate.update("DELETE FROM TB_EMPLOYEE WHERE EMP_ID IN (?, ?, ?)", EMP_ID_1, EMP_ID_2, EMP_ID_3);
    }
}

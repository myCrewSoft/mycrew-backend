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

import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleEmployeeResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleListResponseDTO;

@SpringBootTest
class AdminAuthorizationMapperTest {

    private static final long ROLE_ID = 889930001L;
    private static final long REPLACEMENT_ROLE_ID = 889930002L;
    private static final long PERMISSION_ID = 889940001L;
    private static final long EMP_ID = 889950001L;
    private static final long ROLE_ASSIGN_ID = 889960001L;
    private static final String DEPT_CD = "TADM";
    private static final String JOB_GRD = "TAG01";
    private static final String JOB_PSTN = "TAP01";

    @Autowired
    private AdminAuthorizationMapper adminAuthorizationMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        try {
            cleanUp();
            seedReferenceRows();
            insertPermission(PERMISSION_ID, "TEST_ROLE_MANAGE", "Test role manage");
            insertRole(ROLE_ID, "TEST_ROLE_ADMIN", "Test Role Admin");
            insertRole(REPLACEMENT_ROLE_ID, "TEST_ROLE_REPLACEMENT", "Test Role Replacement");
            insertPermissionMapping(ROLE_ID, PERMISSION_ID);
            insertEmployee();
            insertRoleAssignment(ROLE_ASSIGN_ID, ROLE_ID, EMP_ID, "GLOBAL", "*");
        } catch (DataAccessException e) {
            Assumptions.abort("Oracle test schema is not ready for AdminAuthorizationMapperTest: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try {
            cleanUp();
        } catch (DataAccessException ignored) {
            // Cleanup is best-effort for local integration tests.
        }
    }

    @Test
    void selectPermissionsReturnsPermissionDetails() {
        List<PermissionResponseDTO> permissions = adminAuthorizationMapper.selectPermissions();

        assertThat(permissions)
                .extracting(PermissionResponseDTO::getPermissionCode)
                .contains("TEST_ROLE_MANAGE");
    }

    @Test
    void selectRolesReturnsPermissionAndAssignedEmployeeCounts() {
        List<RoleListResponseDTO> roles = adminAuthorizationMapper.selectRoles();

        RoleListResponseDTO role = roles.stream()
                .filter(candidate -> ROLE_ID == candidate.getRoleId())
                .findFirst()
                .orElseThrow();

        assertThat(role.getPermissionCount()).isEqualTo(1);
        assertThat(role.getAssignedEmployeeCount()).isEqualTo(1);
    }

    @Test
    void selectRoleDetailMapsPermissionsAndAssignedEmployees() {
        RoleDetailResponseDTO role = adminAuthorizationMapper.selectRoleDetail(ROLE_ID);
        List<PermissionResponseDTO> permissions = adminAuthorizationMapper.selectPermissionsByRoleId(ROLE_ID);
        List<RoleEmployeeResponseDTO> employees = adminAuthorizationMapper.selectEmployeesByRoleId(ROLE_ID);

        assertThat(role.getRoleCode()).isEqualTo("TEST_ROLE_ADMIN");
        assertThat(permissions)
                .extracting(PermissionResponseDTO::getPermissionCode)
                .containsExactly("TEST_ROLE_MANAGE");
        assertThat(employees)
                .extracting(RoleEmployeeResponseDTO::getEmpId)
                .containsExactly(EMP_ID);
    }

    @Test
    void insertReplacementRoleAssignmentsSkipsExistingDuplicateScopeAssignment() {
        insertRoleAssignment(ROLE_ASSIGN_ID + 1, REPLACEMENT_ROLE_ID, EMP_ID, "GLOBAL", "*");

        adminAuthorizationMapper.insertReplacementRoleAssignments(ROLE_ID, REPLACEMENT_ROLE_ID);

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM TB_ROLE_ASSIGNMENT
                WHERE ROLE_ID = ?
                  AND EMP_ID = ?
                  AND SCOPE_TYPE_CD = 'GLOBAL'
                  AND SCOPE_ID = '*'
                """, Integer.class, REPLACEMENT_ROLE_ID, EMP_ID);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void insertAndDeleteRoleAssignmentsManageEmployeeRoleAssignment() {
        adminAuthorizationMapper.deleteRoleAssignments(ROLE_ID, List.of(EMP_ID), "GLOBAL", "*");

        adminAuthorizationMapper.insertRoleAssignments(ROLE_ID, List.of(EMP_ID), "GLOBAL", "*");

        Integer insertedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM TB_ROLE_ASSIGNMENT
                WHERE ROLE_ID = ?
                  AND EMP_ID = ?
                  AND SCOPE_TYPE_CD = 'GLOBAL'
                  AND SCOPE_ID = '*'
                  AND ENABLED = 'Y'
                """, Integer.class, ROLE_ID, EMP_ID);
        assertThat(insertedCount).isEqualTo(1);

        adminAuthorizationMapper.deleteRoleAssignments(ROLE_ID, List.of(EMP_ID), "GLOBAL", "*");

        Integer remainingCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM TB_ROLE_ASSIGNMENT
                WHERE ROLE_ID = ?
                  AND EMP_ID = ?
                  AND SCOPE_TYPE_CD = 'GLOBAL'
                  AND SCOPE_ID = '*'
                """, Integer.class, ROLE_ID, EMP_ID);
        assertThat(remainingCount).isZero();
    }

    private void seedReferenceRows() {
        jdbcTemplate.update("""
                MERGE INTO TB_EMP_STAT S
                USING (SELECT 'EMP_ACTIVE' AS EMP_STAT_CD, 'Active' AS EMP_STAT_NM, 'Active employee' AS EMP_STAT_EXPLN FROM DUAL) SRC
                   ON (S.EMP_STAT_CD = SRC.EMP_STAT_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (EMP_STAT_CD, EMP_STAT_NM, EMP_STAT_EXPLN)
                   VALUES (SRC.EMP_STAT_CD, SRC.EMP_STAT_NM, SRC.EMP_STAT_EXPLN)
                """);
        jdbcTemplate.update("""
                MERGE INTO TB_DEPARTMENT D
                USING (SELECT ? AS DEPT_CD, 'Test Admin Dept' AS DEPT_NM FROM DUAL) SRC
                   ON (D.DEPT_CD = SRC.DEPT_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (DEPT_CD, PRNT_DEPT_CD, DEPT_NM, USE_YN, FRST_REG_DT, FRST_RGTR_ID, LAST_MDFCN_DT, LAST_MDFR_ID)
                   VALUES (SRC.DEPT_CD, NULL, SRC.DEPT_NM, 'Y', SYSDATE, 0, NULL, NULL)
                """, DEPT_CD);
        jdbcTemplate.update("""
                MERGE INTO TB_JOB_GRADE G
                USING (SELECT ? AS JOB_GRD_CD, 'Test Admin Grade' AS JOB_GRD_NM FROM DUAL) SRC
                   ON (G.JOB_GRD_CD = SRC.JOB_GRD_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (JOB_GRD_CD, JOB_GRD_NM, USE_YN, FRST_RGTR_ID, FRST_REG_DT, LAST_MDFR_ID, LAST_MDFCN_DT)
                   VALUES (SRC.JOB_GRD_CD, SRC.JOB_GRD_NM, 'Y', 0, SYSDATE, NULL, NULL)
                """, JOB_GRD);
        jdbcTemplate.update("""
                MERGE INTO TB_JOB_POSITION P
                USING (SELECT ? AS JOB_PSTN_CD, 'Test Admin Position' AS JOB_PSTN_NM FROM DUAL) SRC
                   ON (P.JOB_PSTN_CD = SRC.JOB_PSTN_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (JOB_PSTN_CD, JOB_PSTN_NM, USE_YN, FRST_RGTR_ID, FRST_REG_DT, LAST_MDFR_ID, LAST_MDFCN_DT)
                   VALUES (SRC.JOB_PSTN_CD, SRC.JOB_PSTN_NM, 'Y', 0, SYSDATE, NULL, NULL)
                """, JOB_PSTN);
    }

    private void insertPermission(long permissionId, String permissionCode, String permissionName) {
        jdbcTemplate.update("""
                INSERT INTO TB_PERMISSION (
                    PERM_ID,
                    PERM_CD,
                    PERM_NM,
                    PERM_EXPLN,
                    PERM_ENABLED
                )
                VALUES (?, ?, ?, 'Permission for mapper integration test', 'Y')
                """, permissionId, permissionCode, permissionName);
    }

    private void insertRole(long roleId, String roleCode, String roleName) {
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
                VALUES (?, ?, ?, 'Role for mapper integration test', 0, SYSDATE, 0, SYSDATE)
                """, roleId, roleCode, roleName);
    }

    private void insertPermissionMapping(long roleId, long permissionId) {
        jdbcTemplate.update("""
                INSERT INTO TB_PERMISSION_ROLE_MAPPING (PERM_ID, ROLE_ID)
                VALUES (?, ?)
                """, permissionId, roleId);
    }

    private void insertEmployee() {
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
                VALUES (?, 'EMP_ACTIVE', ?, ?, ?, 'Test duty', '{noop}test', 'Role Test Employee',
                        '9901011234567', '1', '010-9999-0001', '12345', 'Test address',
                        'N', SYSDATE, SYSDATE, 'Y')
                """, EMP_ID, JOB_GRD, JOB_PSTN, DEPT_CD);
    }

    private void insertRoleAssignment(
            long roleAssignmentId,
            long roleId,
            long empId,
            String scopeTypeCd,
            String scopeId) {
        jdbcTemplate.update("""
                INSERT INTO TB_ROLE_ASSIGNMENT (
                    ROLE_ASSIGN_ID,
                    ROLE_ID,
                    EMP_ID,
                    SCOPE_TYPE_CD,
                    SCOPE_ID,
                    ENABLED
                )
                VALUES (?, ?, ?, ?, ?, 'Y')
                """, roleAssignmentId, roleId, empId, scopeTypeCd, scopeId);
    }

    private void cleanUp() {
        jdbcTemplate.update("DELETE FROM TB_ROLE_ASSIGNMENT WHERE ROLE_ID IN (?, ?) OR EMP_ID = ?",
                ROLE_ID, REPLACEMENT_ROLE_ID, EMP_ID);
        jdbcTemplate.update("DELETE FROM TB_PERMISSION_ROLE_MAPPING WHERE ROLE_ID IN (?, ?) OR PERM_ID = ?",
                ROLE_ID, REPLACEMENT_ROLE_ID, PERMISSION_ID);
        jdbcTemplate.update("DELETE FROM TB_ROLE WHERE ROLE_ID IN (?, ?)", ROLE_ID, REPLACEMENT_ROLE_ID);
        jdbcTemplate.update("DELETE FROM TB_PERMISSION WHERE PERM_ID = ?", PERMISSION_ID);
        jdbcTemplate.update("DELETE FROM TB_AUTH_VERSION WHERE EMP_ID = ?", EMP_ID);
        jdbcTemplate.update("DELETE FROM TB_EMPLOYEE WHERE EMP_ID = ?", EMP_ID);
        jdbcTemplate.update("DELETE FROM TB_DEPARTMENT WHERE DEPT_CD = ?", DEPT_CD);
        jdbcTemplate.update("DELETE FROM TB_JOB_GRADE WHERE JOB_GRD_CD = ?", JOB_GRD);
        jdbcTemplate.update("DELETE FROM TB_JOB_POSITION WHERE JOB_PSTN_CD = ?", JOB_PSTN);
    }
}

package com.mycrewsoft.domain.jobposition.mapper;

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

import com.mycrewsoft.domain.jobgrade.dto.response.RankResponseDTO;
import com.mycrewsoft.domain.jobgrade.mapper.AdminJobMapper;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;

@SpringBootTest
class AdminJobMapperTest {

    private static final String RANK_ID = "TJGR1";
    private static final String REPLACEMENT_RANK_ID = "TJGR2";
    private static final Long EMP_ID = 889970001L;
    private static final String DEPT_CD = "TJBD";
    private static final String JOB_PSTN = "TJBP";

    @Autowired
    private AdminJobMapper adminJobMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        try {
            cleanUp();
            seedReferenceRows();
        } catch (DataAccessException e) {
            Assumptions.abort("Oracle test schema is not ready for AdminJobMapperTest: " + e.getMessage());
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
    void insertSelectUpdateAndDisableRank() {
        JobGradeVO rank = rank(RANK_ID, "Test Manager", 2);
        adminJobMapper.insertRank(rank);

        RankResponseDTO selected = adminJobMapper.selectRankById(RANK_ID);

        assertThat(selected.getRankName()).isEqualTo("Test Manager");
        assertThat(selected.getSortOrder()).isEqualTo(2);

        adminJobMapper.updateRank(RANK_ID, "Updated Manager", 1, null);

        List<RankResponseDTO> ranks = adminJobMapper.selectRanks();
        RankResponseDTO updated = ranks.stream()
                .filter(candidate -> RANK_ID.equals(candidate.getRankId()))
                .findFirst()
                .orElseThrow();
        assertThat(updated.getRankName()).isEqualTo("Updated Manager");
        assertThat(updated.getSortOrder()).isEqualTo(1);

        adminJobMapper.disableRank(RANK_ID, null);

        assertThat(adminJobMapper.selectRankById(RANK_ID)).isNull();
    }

    @Test
    void updateEmployeeRankTransfersAssignedEmployees() {
        adminJobMapper.insertRank(rank(RANK_ID, "Test Manager", 1));
        adminJobMapper.insertRank(rank(REPLACEMENT_RANK_ID, "Test Staff", 2));
        insertEmployee();

        List<Long> assignedEmpIds = adminJobMapper.selectEmpIdsByRankId(RANK_ID);
        assertThat(assignedEmpIds).containsExactly(EMP_ID);

        adminJobMapper.updateEmployeeRank(RANK_ID, REPLACEMENT_RANK_ID);

        String updatedRankId = jdbcTemplate.queryForObject(
                "SELECT JOB_GRD_CD FROM TB_EMPLOYEE WHERE EMP_ID = ?",
                String.class,
                EMP_ID);
        assertThat(updatedRankId).isEqualTo(REPLACEMENT_RANK_ID);
    }

    @Test
    void updateSelectedEmployeesRankTransfersOnlyRequestedEmployees() {
        adminJobMapper.insertRank(rank(RANK_ID, "Test Manager", 1));
        adminJobMapper.insertRank(rank(REPLACEMENT_RANK_ID, "Test Staff", 2));
        insertEmployee();

        assertThat(adminJobMapper.countEmployeesByRankAndIds(RANK_ID, List.of(EMP_ID))).isEqualTo(1);

        adminJobMapper.updateSelectedEmployeesRank(RANK_ID,List.of(EMP_ID));

        String updatedRankId = jdbcTemplate.queryForObject(
                "SELECT JOB_GRD_CD FROM TB_EMPLOYEE WHERE EMP_ID = ?",
                String.class,
                EMP_ID);
        assertThat(updatedRankId).isEqualTo(REPLACEMENT_RANK_ID);
    }

    private JobGradeVO rank(String rankId, String rankName, int sortOrder) {
        JobGradeVO rank = new JobGradeVO();
        rank.setJobGrdCd(rankId);
        rank.setJobGrdNm(rankName);
        rank.setSortOrder(sortOrder);
        rank.setUseYn("Y");
        return rank;
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
                USING (SELECT ? AS DEPT_CD, 'Test Job Dept' AS DEPT_NM FROM DUAL) SRC
                   ON (D.DEPT_CD = SRC.DEPT_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (DEPT_CD, PRNT_DEPT_CD, DEPT_NM, USE_YN, FRST_REG_DT, FRST_RGTR_ID, LAST_MDFCN_DT, LAST_MDFR_ID)
                   VALUES (SRC.DEPT_CD, NULL, SRC.DEPT_NM, 'Y', SYSDATE, 0, NULL, NULL)
                """, DEPT_CD);
        jdbcTemplate.update("""
                MERGE INTO TB_JOB_POSITION P
                USING (SELECT ? AS JOB_PSTN_CD, 'Test Job Position' AS JOB_PSTN_NM FROM DUAL) SRC
                   ON (P.JOB_PSTN_CD = SRC.JOB_PSTN_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (JOB_PSTN_CD, JOB_PSTN_NM, USE_YN, FRST_RGTR_ID, FRST_REG_DT, LAST_MDFR_ID, LAST_MDFCN_DT)
                   VALUES (SRC.JOB_PSTN_CD, SRC.JOB_PSTN_NM, 'Y', 0, SYSDATE, NULL, NULL)
                """, JOB_PSTN);
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
                VALUES (?, 'EMP_ACTIVE', ?, ?, ?, 'Test duty', '{noop}test', 'Job Test Employee',
                        '9901011234567', '1', '010-9999-0002', '12345', 'Test address',
                        'N', SYSDATE, SYSDATE, 'Y')
                """, EMP_ID, RANK_ID, JOB_PSTN, DEPT_CD);
    }

    private void cleanUp() {
        jdbcTemplate.update("DELETE FROM TB_EMPLOYEE WHERE EMP_ID = ?", EMP_ID);
        jdbcTemplate.update("DELETE FROM TB_JOB_GRADE WHERE JOB_GRD_CD IN (?, ?)", RANK_ID, REPLACEMENT_RANK_ID);
        jdbcTemplate.update("DELETE FROM TB_DEPARTMENT WHERE DEPT_CD = ?", DEPT_CD);
        jdbcTemplate.update("DELETE FROM TB_JOB_POSITION WHERE JOB_PSTN_CD = ?", JOB_PSTN);
    }
}

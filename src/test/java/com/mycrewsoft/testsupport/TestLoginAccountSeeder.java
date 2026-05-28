package com.mycrewsoft.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestLoginAccountSeeder {

    public static final long EMP_ID = 990000001L;
    public static final String PASSWORD = "test-login-password";

    private static final String EMP_STAT_CD = "EMP_INITIAL";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public TestLoginAccountSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public void resetTestAccount() {
        deleteTestAccount();
        insertInitialEmployeeStatus();
        insertTestEmployee();
    }

    public void deleteTestAccount() {
        jdbcTemplate.update("DELETE FROM TB_ROLE_ASSIGNMENT WHERE EMP_ID = ?", EMP_ID);
        jdbcTemplate.update("DELETE FROM TB_AUTH_VERSION WHERE EMP_ID = ?", EMP_ID);
        jdbcTemplate.update("DELETE FROM TB_EMPLOYEE WHERE EMP_ID = ?", EMP_ID);
    }

    private void insertInitialEmployeeStatus() {
        jdbcTemplate.update("""
                MERGE INTO TB_EMP_STAT S
                USING (
                    SELECT ? AS EMP_STAT_CD,
                           'Initial account' AS EMP_STAT_NM,
                           'Test login account status' AS EMP_STAT_EXPLN
                    FROM DUAL
                ) SRC
                   ON (S.EMP_STAT_CD = SRC.EMP_STAT_CD)
                 WHEN NOT MATCHED THEN
                   INSERT (EMP_STAT_CD, EMP_STAT_NM, EMP_STAT_EXPLN)
                   VALUES (SRC.EMP_STAT_CD, SRC.EMP_STAT_NM, SRC.EMP_STAT_EXPLN)
                """, EMP_STAT_CD);
    }

    private void insertTestEmployee() {
        jdbcTemplate.update("""
                INSERT INTO TB_EMPLOYEE (
                    EMP_ID,
                    EMP_STAT_CD,
                    PSWD,
                    EMP_NM,
                    RRNO,
                    GENDER_CD,
                    MBL_TELNO,
                    ZIP,
                    ADDR,
                    ENTCO_YMD,
                    FRST_REG_DT,
                    ENABLED
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    'Test Login User',
                    '9901011234567',
                    '1',
                    '010-0000-0000',
                    '12345',
                    'Test address',
                    SYSDATE,
                    SYSDATE,
                    'Y'
                )
                """,
                EMP_ID,
                EMP_STAT_CD,
                passwordEncoder.encode(PASSWORD));
    }
}

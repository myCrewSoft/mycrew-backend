package com.mycrewsoft.domain.jobgrade.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Order(0)
@RequiredArgsConstructor
public class JobGradeSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Integer columnCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM USER_TAB_COLUMNS
                WHERE TABLE_NAME = 'TB_JOB_GRADE'
                  AND COLUMN_NAME = 'SORT_ORDER'
                """, Integer.class);

        if (columnCount != null && columnCount == 0) {
            jdbcTemplate.execute("""
                    ALTER TABLE TB_JOB_GRADE
                    ADD (SORT_ORDER NUMBER(10) DEFAULT 0 NOT NULL)
                    """);
        }
    }
}

package com.mycrewsoft.domain.jobgrade.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class AdminJobMapperXmlTest {

    @Test
    void selectNextRankCodeUsesFiveCharacterJobGradeCode() throws Exception {
        String selectSql = selectNextRankCodeSql();

        assertThat(selectSql)
                .contains("'JOB' || LPAD")
                .contains("SUBSTR(TRIM(JOB_GRD_CD), 4)")
                .contains("REGEXP_LIKE(TRIM(JOB_GRD_CD), '^JOB[0-9]+$')")
                .contains("2,\n            '0'");
        assertThat(selectSql).doesNotContain("'RANK_'");
    }

    private String selectNextRankCodeSql() throws Exception {
        String mapperXml = Files.readString(
                Path.of("src/main/resources/mapper/jobgrade/AdminJobMapper.xml"),
                StandardCharsets.UTF_8);
        Matcher matcher = Pattern
                .compile("(?s)<select id=\"selectNextRankCode\"[^>]*>(.*?)</select>")
                .matcher(mapperXml);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}

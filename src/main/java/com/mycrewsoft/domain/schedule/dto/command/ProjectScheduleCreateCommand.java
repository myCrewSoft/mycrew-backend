package com.mycrewsoft.domain.schedule.dto.command;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectScheduleCreateCommand {

    private Long projId;              // 프로젝트 ID
    private String projNm;            // 프로젝트명

    private LocalDate projBgngYmd;    // 프로젝트 시작일
    private LocalDate projEndYmd;     // 프로젝트 종료일

    private Long crtrId;              // 생성자 ID
    private List<Long> empIds;        // 일정 대상 사원 ID 목록
}
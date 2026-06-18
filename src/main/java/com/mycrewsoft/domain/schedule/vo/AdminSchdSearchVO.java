package com.mycrewsoft.domain.schedule.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSchdSearchVO {

    private LocalDateTime beginDt;
    private LocalDateTime endDt;

    private List<String> schdClsfCdList;

    private String keyword;
}
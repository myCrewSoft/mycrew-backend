package com.mycrewsoft.domain.board.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardWidgetVO {

    private Long boardId;
    private String boardSj;
    private String empNm;
    private LocalDateTime frstRegDt;
}
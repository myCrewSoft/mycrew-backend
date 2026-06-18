package com.mycrewsoft.domain.room.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfRmListItem {

    private Long confRmId;
    private String confRmNm;
    private int confRmFlr;
    private String confRmHo;
    private String confRmColor;
    private String useYn;
    private String mngrNm;
    private String mngrDeptNm;
    private String mngrJobGrdNm;
    private Long mngrPrflImgFileId;
    private Long confRmMngrId;
}
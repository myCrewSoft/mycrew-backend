package com.mycrewsoft.domain.room.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfRmAdminVO {

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
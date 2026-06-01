package com.mycrewsoft.domain.schedule.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TB_SCHD_TARGET + JOIN 결과를 담는 VO.
 * selectById 단건 조회 시 참여자 상세 정보를 함께 반환할 때 사용한다.
 */
@Getter
@NoArgsConstructor
public class SchdTargetDetailVO {

    private String targetTypeCd;
    private String targetId;

    /** targetTypeCd='02'(개인) → 사원명, '04'(부서) → 부서명, '05'(프로젝트) → 프로젝트명, '06'(업무) → 업무명 */
    private String targetNm;

    /** targetTypeCd='02'(개인)일 때만 사용 */
    private String deptNm;

    /** targetTypeCd='02'(개인)일 때만 사용 */
    private String jobGrdNm;

    /** targetTypeCd='02'(개인)일 때만 사용 */
    private String profileImgUrl;
}
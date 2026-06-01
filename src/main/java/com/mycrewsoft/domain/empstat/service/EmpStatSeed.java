package com.mycrewsoft.domain.empstat.service;

import com.mycrewsoft.domain.empstat.code.EmpStatCode;

/**
 * EmpStatCode enum을 TB_EMP_STAT 코드 데이터로 동기화할 때 사용하는 값 객체.
 */
public record EmpStatSeed(
        String code,
        String name,
        String description) {

    public static EmpStatSeed from(EmpStatCode empStatCode) {
        return new EmpStatSeed(
                empStatCode.getCode(),
                empStatCode.getLabel(),
                empStatCode.getLabel());
    }
}

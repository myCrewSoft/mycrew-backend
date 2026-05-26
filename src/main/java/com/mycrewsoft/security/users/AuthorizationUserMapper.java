package com.mycrewsoft.security.users;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 로그인 가능한 사용자를 조회하는 MyBatis Mapper.
 * 실제 SQL은 src/main/resources/mapper/security/AuthorizationUserMapper.xml 에 정의한다.
 *
 * 역할:
 * - 로그인 ID로 사원 계정을 조회한다.
 * - Spring Security 인증에 필요한 최소 사용자 정보를 AuthorizationUserRecord로 반환한다.
 *
 * 주의:
 * - 비밀번호 검증은 이 Mapper가 하지 않는다.
 * - 계정 활성 여부 컬럼은 실제 TB_EMPLOYEE 스키마에 맞춰 XML에서 관리한다.
 */
@Mapper
public interface AuthorizationUserMapper {

    AuthorizationUserRecord selectLoginUserByUsername(@Param("username") String username);
}

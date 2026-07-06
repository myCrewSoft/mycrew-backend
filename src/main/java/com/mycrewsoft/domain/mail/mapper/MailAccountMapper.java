package com.mycrewsoft.domain.mail.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.mail.vo.MailAccountVO;

@Mapper
public interface MailAccountMapper {

    int upsertGoogleMailAccount(MailAccountVO mailAccount);

    /**
     * 같은 구글 계정(google_sub)을 점유하고 있는 다른 직원의 행을 해제한다.
     * (한 구글 계정은 한 직원만 연동) 유니크 제약 충돌 방지를 위해 google_sub 값을 비우고 REVOKED 처리한다.
     */
    int releaseGoogleSubFromOtherEmployees(
            @Param("googleSubId") String googleSubId,
            @Param("empId") Long empId);

    int existsActiveMailAccount(Long empId);

    int existsActiveGoogleMailAccountByEmail(
            @Param("empId") Long empId,
            @Param("emailAddr") String emailAddr);
}

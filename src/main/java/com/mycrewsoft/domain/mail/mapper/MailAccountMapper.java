package com.mycrewsoft.domain.mail.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.mail.vo.MailAccountVO;

@Mapper
public interface MailAccountMapper {

    int upsertGoogleMailAccount(MailAccountVO mailAccount);

    int existsActiveMailAccount(Long empId);

    int existsActiveGoogleMailAccountByEmail(
            @Param("empId") Long empId,
            @Param("emailAddr") String emailAddr);
}

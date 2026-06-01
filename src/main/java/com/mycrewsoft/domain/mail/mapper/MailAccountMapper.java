package com.mycrewsoft.domain.mail.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.mail.vo.MailAccountVO;

@Mapper
public interface MailAccountMapper {

    int upsertGoogleMailAccount(MailAccountVO mailAccount);

    int existsActiveMailAccount(Long empId);
}

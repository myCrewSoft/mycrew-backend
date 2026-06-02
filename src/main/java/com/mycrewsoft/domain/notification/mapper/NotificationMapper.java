package com.mycrewsoft.domain.notification.mapper;

import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.notification.vo.AlrmVO;

@Mapper
public interface NotificationMapper {

    List<AlrmVO> selectAlrmList(@Param("rcvrEmpId") Long rcvrEmpId);

    long selectUnreadCount(@Param("rcvrEmpId") Long rcvrEmpId);

    int insertAlrm(AlrmVO alrmVO);

    int insertAlrmRcvr(AlrmRcvrVO alrmRcvrVO);
    
    int updateReadAll(@Param("rcvrEmpId") Long rcvrEmpId);

    int updateDelYn(@Param("alrmRcvrId") Long alrmRcvrId, @Param("rcvrEmpId") Long rcvrEmpId);
} 


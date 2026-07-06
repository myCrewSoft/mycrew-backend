package com.mycrewsoft.domain.notification.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.notification.vo.AlrmVO;
import com.mycrewsoft.domain.notification.vo.NotificationQueryVO;

@Mapper
public interface NotificationMapper {

    long selectUnreadCount(@Param("rcvrEmpId") Long rcvrEmpId);

    int insertAlrm(AlrmVO alrmVO);

    int insertAlrmRcvr(AlrmRcvrVO alrmRcvrVO);
    
    int updateReadAll(@Param("rcvrEmpId") Long rcvrEmpId);

    int updateRead(@Param("alrmRcvrId") Long alrmRcvrId, @Param("rcvrEmpId") Long rcvrEmpId);

    int updateDelYn(@Param("alrmRcvrId") Long alrmRcvrId, @Param("rcvrEmpId") Long rcvrEmpId);
    
    // 위젯용
    List<NotificationQueryVO> selectAlrmList(@Param("empId") Long empId, @Param("limit") int limit);
} 


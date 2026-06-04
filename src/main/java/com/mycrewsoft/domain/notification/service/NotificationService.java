package com.mycrewsoft.domain.notification.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.mycrewsoft.domain.notification.vo.AlrmVO;

public interface NotificationService {

    List<NotificationResponse> readAlrmList();

    NotificationUnreadCountResponse readUnreadCount();

    void sendAlrm(AlrmVO alrmVO, List<Long> rcvrEmpIds);

    void sendAlrm(String ttln, String typeCd, String cn, List<Long> rcvrEmpIds);

    void readAllAlrm();

    void deleteAlrm(Long alrmRcvrId);
}

package com.mycrewsoft.domain.room.service;

import java.util.List;

import com.mycrewsoft.domain.room.dto.response.ConfRmListItem;
import com.mycrewsoft.domain.room.dto.response.ConfRmStatsSummary;

public interface ConfRmAdminService {

	ConfRmStatsSummary readConfRmStatsSummary();

	List<ConfRmListItem> readConfRmList();
}
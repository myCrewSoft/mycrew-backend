package com.mycrewsoft.domain.reservation.service;

import java.util.List;

import com.mycrewsoft.domain.reservation.dto.request.RsrvSearchRequest;
import com.mycrewsoft.domain.reservation.dto.response.RsrvListItem;
import com.mycrewsoft.domain.reservation.dto.response.RsrvStatsSummary;

public interface RsrvAdminService {

	RsrvStatsSummary readRsrvStatsSummary();

	List<RsrvListItem> readRsrvList(RsrvSearchRequest request);

	void cancelRsrv(Long rsrvId);
}
package com.mycrewsoft.domain.reservation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
import com.mycrewsoft.domain.reservation.dto.request.ReservationUpdateRequest;
import com.mycrewsoft.domain.reservation.dto.response.ReservationResponse;
import com.mycrewsoft.domain.reservation.vo.ConfRmRsrvVO;
import com.mycrewsoft.domain.reservation.vo.ReservationDetailVO;

@Mapper(componentModel = "spring")
public interface ReservationDtoMapper {

	@Mapping(source = "rsrvId",              target = "reservationId")
	@Mapping(source = "confRmId",            target = "roomId")
	@Mapping(source = "rsrvPurps",           target = "title")
	@Mapping(source = "rsrvEmpId",           target = "reserverId")
	@Mapping(source = "rsrvEmpNm",           target = "reserverName")
	@Mapping(source = "rsrvEmpDeptCd",       target = "rsrvEmpDeptCd")
	@Mapping(source = "rsrvEmpJobGrdCd",     target = "rsrvEmpJobGrdCd")
	@Mapping(source = "rsrvEmpPrflImgFileId", target = "rsrvEmpPrflImgFileId")
	@Mapping(source = "beginDt",             target = "startDateTime")
	@Mapping(source = "endDt",               target = "endDateTime")
	@Mapping(target = "mine",                ignore = true)
	ReservationResponse toResponse(ReservationDetailVO detailVO);

	List<ReservationResponse> toResponseList(List<ReservationDetailVO> detailVOList);

    @Mapping(source = "roomId",        target = "confRmId")
    @Mapping(source = "title",         target = "rsrvPurps")
    @Mapping(source = "startDateTime", target = "beginDt")
    @Mapping(source = "endDateTime",   target = "endDt")
    @Mapping(target = "rsrvId",        ignore = true)
    @Mapping(target = "rsrvEmpId",     ignore = true)
    @Mapping(target = "delYn",   ignore = true)
    ConfRmRsrvVO toVo(ReservationCreateRequest request);

    @Mapping(source = "roomId",        target = "confRmId")
    @Mapping(source = "title",         target = "rsrvPurps")
    @Mapping(source = "startDateTime", target = "beginDt")
    @Mapping(source = "endDateTime",   target = "endDt")
    @Mapping(target = "rsrvId",        ignore = true)
    @Mapping(target = "rsrvEmpId",     ignore = true)
    @Mapping(target = "delYn",   ignore = true)
    ConfRmRsrvVO toVo(ReservationUpdateRequest request);
}
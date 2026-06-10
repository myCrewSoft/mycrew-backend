package com.mycrewsoft.domain.video.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycrewsoft.domain.video.dto.request.VideoConfCreateRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomUpdateRequest;
import com.mycrewsoft.domain.video.dto.response.VideoConfResponse;
import com.mycrewsoft.domain.video.dto.response.VideoMomAprvlResponse;
import com.mycrewsoft.domain.video.dto.response.VideoMomResponse;
import com.mycrewsoft.domain.video.dto.response.VideoPtcptResponse;
import com.mycrewsoft.domain.video.vo.VideoConfVO;
import com.mycrewsoft.domain.video.vo.VideoMomAprvlVO;
import com.mycrewsoft.domain.video.vo.VideoMomVO;
import com.mycrewsoft.domain.video.vo.VideoPtcptVO;

@Mapper(componentModel = "spring")
public interface VideoConfDtoMapper {

    // ── Request → VO ──────────────────────────────────────────────

    // 화상회의 생성 요청 DTO → VO (crtrId, roomNm은 Service에서 별도 세팅)
    @Mapping(target = "vconfId",    ignore = true)
    @Mapping(target = "crtrId",     ignore = true)
    @Mapping(target = "roomNm",     ignore = true)
    @Mapping(target = "confSttusCd", ignore = true)
    @Mapping(target = "creatDt",    ignore = true)
    @Mapping(target = "videoPtcpt", ignore = true)
    @Mapping(target = "videoRcrdg", ignore = true)
    @Mapping(target = "videoChatLog", ignore = true)
    @Mapping(target = "videoMom",   ignore = true)
    VideoConfVO toConfVO(VideoConfCreateRequest request);

    // 회의록 수정 요청 DTO → VO (momId, vconfId 등은 Service에서 별도 세팅)
    @Mapping(target = "momId",        ignore = true)
    @Mapping(target = "vconfId",      ignore = true)
    @Mapping(target = "momSttusCd",   ignore = true)
    @Mapping(target = "edtrId",       ignore = true)
    @Mapping(target = "creatDt",      ignore = true)
    @Mapping(target = "delDt",        ignore = true)
    @Mapping(target = "revwReqDt",    ignore = true)
    @Mapping(target = "cnfrmDt",      ignore = true)
    @Mapping(target = "aprvlRoundNo", ignore = true)
    @Mapping(target = "videoMomAprvl", ignore = true)
    @Mapping(target = "videoMomHist",  ignore = true)
    VideoMomVO toMomVO(VideoMomUpdateRequest request);

    // ── VO → Response ─────────────────────────────────────────────

    // 화상회의 VO → 응답 DTO
    @Mapping(source = "videoPtcpt", target = "ptcptList")
    VideoConfResponse toConfResponse(VideoConfVO vo);

    // 화상회의 VO 목록 → 응답 DTO 목록
    List<VideoConfResponse> toConfResponseList(List<VideoConfVO> voList);

    // 참여자 VO → 응답 DTO
    VideoPtcptResponse toPtcptResponse(VideoPtcptVO vo);

    // 회의록 VO → 응답 DTO
    @Mapping(source = "videoMomAprvl", target = "aprvlList")
    VideoMomResponse toMomResponse(VideoMomVO vo);

    // 회의록 결재 VO → 응답 DTO
    VideoMomAprvlResponse toMomAprvlResponse(VideoMomAprvlVO vo);
}
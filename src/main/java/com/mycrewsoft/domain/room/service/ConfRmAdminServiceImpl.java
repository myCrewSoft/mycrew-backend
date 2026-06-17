package com.mycrewsoft.domain.room.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.room.dto.response.ConfRmListItem;
import com.mycrewsoft.domain.room.dto.response.ConfRmStatsSummary;
import com.mycrewsoft.domain.room.mapper.ConfRmAdminMapper;
import com.mycrewsoft.domain.room.mapper.RoomDtoMapper;
import com.mycrewsoft.domain.room.vo.ConfRmAdminVO;
import com.mycrewsoft.domain.room.vo.ConfRmSummaryVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfRmAdminServiceImpl implements ConfRmAdminService {

    private final ConfRmAdminMapper confRmAdminMapper;
    private final RoomDtoMapper roomDtoMapper;

    @Override
    public ConfRmStatsSummary readConfRmStatsSummary() {
        ConfRmSummaryVO vo = confRmAdminMapper.selectConfRmStatsSummary();
        return roomDtoMapper.toConfRmStatsSummary(vo);
    }

    @Override
    public List<ConfRmListItem> readConfRmList() {
        List<ConfRmAdminVO> voList = confRmAdminMapper.selectConfRmList();
        return voList.stream()
                .map(roomDtoMapper::toConfRmListItem)
                .toList();
    }
}
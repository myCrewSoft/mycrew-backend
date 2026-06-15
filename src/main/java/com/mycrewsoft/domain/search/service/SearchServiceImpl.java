package com.mycrewsoft.domain.search.service;

import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.mapper.SearchDtoMapper;
import com.mycrewsoft.domain.search.mapper.SearchMapper;
import com.mycrewsoft.domain.search.vo.RecentItemVO;
import com.mycrewsoft.domain.search.vo.SearchHistVO;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final SearchMapper searchMapper;
    private final SearchDtoMapper searchDtoMapper;
    
    @Override
    @Transactional
    public List<SearchResponse> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        
        Long empId = getCurrentEmpIdOrThrow();

        saveSearchHist(keyword, empId);

        return searchMapper.search(keyword, empId)
                .stream()
                .map(searchDtoMapper::toResponse)
                .toList();
    }

    @Override
    public List<SearchHistVO> readSearchHist() {
        return searchMapper.readSearchHist(getCurrentEmpIdOrThrow());
    }

    @Override
    @Transactional
    public void deleteSearchHist(Long searchHistId) {
        searchMapper.deleteSearchHist(searchHistId);
    }

    @Override
    @Transactional
    public void deleteAllSearchHist() {
        searchMapper.deleteAllSearchHist(getCurrentEmpIdOrThrow());
    }

    @Override
    @Transactional
    public void mergeRecentItem(SearchType itemType, Long itemId) {
        RecentItemVO vo = new RecentItemVO();
        vo.setEmpId(getCurrentEmpIdOrThrow());
        vo.setItemType(itemType.name());
        vo.setItemId(itemId);
        searchMapper.mergeRecentItem(vo);
    }

    @Override
    public List<RecentItemVO> readRecentItems() {
        return searchMapper.readRecentItems(getCurrentEmpIdOrThrow());
    }

    @Override
    @Transactional
    public void deleteRecentItem(Long recentItemId) {
        searchMapper.deleteRecentItem(recentItemId);
    }

    private void saveSearchHist(String keyword, Long empId) {
        SearchHistVO vo = new SearchHistVO();
        vo.setEmpId(empId);
        vo.setKeyword(keyword);
        searchMapper.insertSearchHist(vo);
    }

    private Long getCurrentEmpIdOrThrow() {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (empId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return empId;
    }
}
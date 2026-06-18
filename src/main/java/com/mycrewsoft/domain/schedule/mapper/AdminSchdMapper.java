package com.mycrewsoft.domain.schedule.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.schedule.vo.AdminSchdListVO;
import com.mycrewsoft.domain.schedule.vo.AdminSchdSearchVO;
import com.mycrewsoft.domain.schedule.vo.IntgSchdVO;

@Mapper
public interface AdminSchdMapper {

    List<AdminSchdListVO> selectAdminSchdList(
        @Param("search") AdminSchdSearchVO search,
        @Param("offset") long offset,
        @Param("size") int size
    );

    long countAdminSchdList(@Param("search") AdminSchdSearchVO search);

    IntgSchdVO selectAdminSchd(@Param("schdId") Long schdId);

    int insertAdminSchd(IntgSchdVO vo);

    int updateAdminSchd(IntgSchdVO vo);

    int deleteAdminSchd(@Param("schdId") Long schdId);
}
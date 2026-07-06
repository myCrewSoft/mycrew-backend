package com.mycrewsoft.domain.department.mapper;

import com.mycrewsoft.domain.department.vo.DepartmentLookupVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentLookupMapper {

    List<DepartmentLookupVO> selectDepartmentsForLookup();
}
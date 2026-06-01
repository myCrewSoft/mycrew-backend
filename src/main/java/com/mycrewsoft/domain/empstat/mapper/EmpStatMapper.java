package com.mycrewsoft.domain.empstat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.empstat.service.EmpStatSeed;

@Mapper
public interface EmpStatMapper {

    int mergeEmpStats(@Param("empStats") List<EmpStatSeed> empStats);
}

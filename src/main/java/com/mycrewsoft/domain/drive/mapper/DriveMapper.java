package com.mycrewsoft.domain.drive.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.drive.vo.DriveVo;

@Mapper
public interface DriveMapper {
	
	int insertDriveItem(DriveVo vo);
}

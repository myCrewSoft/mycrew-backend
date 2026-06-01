package com.mycrewsoft.domain.drive.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.drive.vo.DriveVo;

@Mapper
public interface DriveMapper {
	
	/**
	 * 드라이브 아이템 등록
	 * @param vo
	 * @return
	 */
	int insertDriveItem(DriveVo vo);
	
	/** 
	 * 개인 드라이브 전체 목록 조회
	 * @param empId
	 * @return
	 */
	List<DriveVo> selectListByEmpId(Long empId);
}

package com.mycrewsoft.domain.drive.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
	List<DriveVo> selectListByEmpId(@Param("empId") Long empId, @Param("prntDriveItemId") Long prntDriveItemId);
	
	/**
	 * 드라이브 아이템 단건 조회
	 * @param driveItemId
	 * @return
	 */
	DriveVo selectDriveItemById(Long driveItemId);
	
	/**
	 * 드라이브 폴더명 수정
	 * @param driveItemId
	 * @param ItemNm
	 * @return
	 */
	int updateFolderName(@Param("driveItemId") Long driveItemId, @Param("itemNm") String ItemNm);
	
	/**
	 * 즐겨찾기 등록/해제
	 * @param driveItemId
	 * @return
	 */
	int updateBookmarkYn(@Param("driveItemId") Long driveItemId, @Param("newBookmarkYn") String newBookmarkYn);
	
	/**
	 * 드라이브 단건 삭제상태 변경 (부모 아이템 삭제 시 하위 아이템까지 모두 삭제 상태 변경)
	 * @param driveItemId
	 * @param deltrMbrId
	 * @return
	 */
	int softDeleteItemWithChildren(@Param("driveItemId") Long driveItemId, @Param("deltrMbrId") Long deltrMbrId);

	/**
	 * 폴더 계층 조회
	 * @param driveItemId
	 * @return
	 */
	List<DriveVo> selectFileChildrenByItemId(Long driveItemId);
}

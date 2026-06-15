package com.mycrewsoft.domain.jobgrade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.jobgrade.dto.response.RankResponseDTO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;

@Mapper
public interface AdminJobMapper {

    List<RankResponseDTO> selectRanks();

    RankResponseDTO selectRankById(@Param("rankId") String rankId);

    int insertRank(JobGradeVO rank);

    int updateRank(
            @Param("rankId") String rankId,
            @Param("rankName") String rankName,
            @Param("sortOrder") Integer sortOrder,
            @Param("lastModifierId") Long lastModifierId);

    int disableRank(
            @Param("rankId") String rankId,
            @Param("lastModifierId") Long lastModifierId);

    List<Long> selectEmpIdsByRankId(@Param("rankId") String rankId);

    int updateEmployeeRank(
            @Param("rankId") String rankId,
            @Param("replacementRankId") String replacementRankId);

    int countEnabledEmployeesByIds(@Param("empIds") List<Long> empIds);

    int countEmployeesByRankAndIds(
            @Param("rankId") String rankId,
            @Param("empIds") List<Long> empIds);

    int updateEmployeesRank(
            @Param("rankId") String rankId,
            @Param("empIds") List<Long> empIds);

    int updateSelectedEmployeesRank(
            @Param("rankId") String rankId,
            @Param("empIds") List<Long> empIds);
}

package com.mycrewsoft.domain.room.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import com.mycrewsoft.domain.room.dto.response.ConfRmListItem;
import com.mycrewsoft.domain.room.dto.response.ConfRmStatsSummary;
import com.mycrewsoft.domain.room.mapper.ConfRmAdminMapper;
import com.mycrewsoft.domain.room.mapper.RoomDtoMapper;
import com.mycrewsoft.domain.room.vo.ConfRmAdminVO;
import com.mycrewsoft.domain.room.vo.ConfRmSummaryVO;

@ExtendWith(MockitoExtension.class)
class ConfRmAdminServiceTest {

	@InjectMocks
	private ConfRmAdminServiceImpl confRmAdminService;

	@Mock
	private ConfRmAdminMapper confRmAdminMapper;

	@Mock
	private RoomDtoMapper roomDtoMapper;

	private ConfRmSummaryVO summaryVO;
	private ConfRmStatsSummary summaryDto;
	private ConfRmAdminVO adminVO;
	private ConfRmListItem listItemDto;

	@BeforeEach
	void setUp() {
		summaryVO = new ConfRmSummaryVO();
		setField(summaryVO, "totalCount", 10);
		setField(summaryVO, "inUseCount", 3);
		setField(summaryVO, "availableCount", 5);
		setField(summaryVO, "avgOccupancyRate", 62.5);

		summaryDto = ConfRmStatsSummary.builder().totalCount(10).inUseCount(3).availableCount(5).avgOccupancyRate(62.5)
				.build();

		adminVO = new ConfRmAdminVO();
		setField(adminVO, "confRmId", 1L);
		setField(adminVO, "confRmNm", "대회의실");
		setField(adminVO, "confRmFlr", 3);
		setField(adminVO, "confRmHo", "301");
		setField(adminVO, "useYn", "Y");
		setField(adminVO, "mngrNm", "김철수");
		setField(adminVO, "mngrDeptNm", "총무팀");
		setField(adminVO, "mngrJobGrdNm", "대리");

		listItemDto = ConfRmListItem.builder().confRmId(1L).confRmNm("대회의실").confRmFlr(3).confRmHo("301").useYn("Y")
				.mngrNm("김철수").mngrDeptNm("총무팀").mngrJobGrdNm("대리").build();
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void 운영_현황_요약_조회_성공() {
		given(confRmAdminMapper.selectConfRmStatsSummary()).willReturn(summaryVO);
		given(roomDtoMapper.toConfRmStatsSummary(summaryVO)).willReturn(summaryDto);

		ConfRmStatsSummary result = confRmAdminService.readConfRmStatsSummary();

		assertThat(result.getTotalCount()).isEqualTo(10);
		assertThat(result.getInUseCount()).isEqualTo(3);
		assertThat(result.getAvailableCount()).isEqualTo(5);
		assertThat(result.getAvgOccupancyRate()).isEqualTo(62.5);
	}

	@Test
	void 회의실_목록_조회_성공() {
		given(confRmAdminMapper.selectConfRmList()).willReturn(List.of(adminVO));
		given(roomDtoMapper.toConfRmListItem(adminVO)).willReturn(listItemDto);

		List<ConfRmListItem> result = confRmAdminService.readConfRmList();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getConfRmNm()).isEqualTo("대회의실");
		assertThat(result.get(0).getMngrNm()).isEqualTo("김철수");
	}

	@Test
	void 회의실_목록_없을때_빈_리스트_반환() {
		given(confRmAdminMapper.selectConfRmList()).willReturn(List.of());

		List<ConfRmListItem> result = confRmAdminService.readConfRmList();

		assertThat(result).isEmpty();
	}
}
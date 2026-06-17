package com.mycrewsoft.domain.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 근무 정책 저장(신규 버전 생성) 요청 DTO.
 * 관리자가 근태 관리 탭에서 회사 근무 정책을 설정할 때 사용한다.
 */
@Getter
@Setter
@Schema(description = "근무 정책 저장 요청 DTO")
public class AtndPolicySaveRequest {

	@Schema(description = "정책명", example = "2026년 표준 근무 정책")
	@NotBlank(message = "정책명은 필수입니다.")
	private String policyNm;

	@Schema(description = "출근 기준 시각(HH:mm)", example = "09:00")
	@NotBlank(message = "출근 기준 시각은 필수입니다.")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "출근 기준 시각은 HH:mm 형식이어야 합니다.")
	private String workBgnTm;

	@Schema(description = "퇴근 기준 시각(HH:mm)", example = "18:00")
	@NotBlank(message = "퇴근 기준 시각은 필수입니다.")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "퇴근 기준 시각은 HH:mm 형식이어야 합니다.")
	private String workEndTm;

	@Schema(description = "휴게 시간(분)", example = "60")
	@NotNull(message = "휴게 시간은 필수입니다.")
	@Min(value = 0, message = "휴게 시간은 0 이상이어야 합니다.")
	private Integer breakMin;

	@Schema(description = "지각 허용 시간(분)", example = "0")
	@NotNull(message = "지각 허용 시간은 필수입니다.")
	@Min(value = 0, message = "지각 허용 시간은 0 이상이어야 합니다.")
	private Integer lateGraceMin;

	@Schema(description = "1일 소정근로(분)", example = "480")
	@NotNull(message = "1일 소정근로는 필수입니다.")
	@Min(value = 1, message = "1일 소정근로는 1 이상이어야 합니다.")
	private Integer stdWorkMinDay;

	@Schema(description = "주 소정근로일", example = "5")
	@NotNull(message = "주 소정근로일은 필수입니다.")
	@Min(value = 1, message = "주 소정근로일은 1 이상이어야 합니다.")
	private Integer stdWorkDaysWk;

	@Schema(description = "주 소정근로(분)", example = "2400")
	@NotNull(message = "주 소정근로는 필수입니다.")
	@Min(value = 1, message = "주 소정근로는 1 이상이어야 합니다.")
	private Integer stdWorkMinWk;

	@Schema(description = "주 연장근무 한도(분)", example = "720")
	@NotNull(message = "주 연장근무 한도는 필수입니다.")
	@Min(value = 0, message = "주 연장근무 한도는 0 이상이어야 합니다.")
	private Integer maxOtMinWk;

	@Schema(description = "연장 인정 단위(분)", example = "30")
	@Min(value = 1, message = "연장 인정 단위는 1 이상이어야 합니다.")
	private Integer otUnitMin;

	@Schema(description = "기본 연차(일)", example = "15")
	@NotNull(message = "기본 연차는 필수입니다.")
	@Min(value = 0, message = "기본 연차는 0 이상이어야 합니다.")
	private Double annualLeaveDef;
}

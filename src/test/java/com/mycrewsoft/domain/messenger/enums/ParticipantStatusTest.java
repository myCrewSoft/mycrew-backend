package com.mycrewsoft.domain.messenger.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;

class ParticipantStatusTest {

    @Test
    void fromCode_returnsStatusAfterTrimmingPadding() {
        assertThat(ParticipantStatus.fromCode(ParticipantStatus.ONLINE.getCode() + " "))
                .isEqualTo(ParticipantStatus.ONLINE);
    }

    @Test
    void fromCode_rejectsUnknownCode() {
        assertThatThrownBy(() -> ParticipantStatus.fromCode("UNKNOWN"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }
}

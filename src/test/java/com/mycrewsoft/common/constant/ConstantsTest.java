package com.mycrewsoft.common.constant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void publicUrlsIncludeActuatorMonitoringEndpoints() {
        assertThat(Constants.PUBLIC_URLS)
                .contains(
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/prometheus");
    }
}

package com.mycrewsoft.domain.video.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingReminderVO {
    private Long vconfId;
    private Long meetingId;
    private String vconfNm;
    private List<Long> empIds;
}

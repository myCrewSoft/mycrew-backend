package com.mycrewsoft.domain.video.config;

import io.livekit.server.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKitRoomService {

    private final LiveKitProperties liveKitProperties;

    public void deleteRoom(String roomName) {
        try {
            RoomServiceClient client = RoomServiceClient.createClient(
                    liveKitProperties.getUrl(),
                    liveKitProperties.getApiKey(),
                    liveKitProperties.getApiSecret()
            );
            client.deleteRoom(roomName).execute();
            log.info("[LiveKit] 방 삭제 완료 - roomName: {}", roomName);
        } catch (Exception e) {
            log.error("[LiveKit] 방 삭제 실패 - roomName: {}", roomName, e);
        }
    }
}

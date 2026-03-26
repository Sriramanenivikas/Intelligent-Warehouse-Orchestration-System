package com.iwos.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private String eventId;
    private String userId;
    private String channel;
    private String templateCode;
    private Map<String, String> templateParams;
    private Instant timestamp;
}

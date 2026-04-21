package com.iwos.notification.application;

import com.iwos.notification.api.http.NotificationResponse;
import com.iwos.notification.domain.notification.NotificationAudience;
import com.iwos.notification.domain.notification.NotificationNotFoundException;
import com.iwos.notification.infrastructure.persistence.NotificationResponseMapper;
import com.iwos.notification.infrastructure.persistence.entity.NotificationEntity;
import com.iwos.notification.infrastructure.persistence.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationResponseMapper mapper;

    public NotificationQueryService(
            NotificationRepository notificationRepository,
            NotificationResponseMapper mapper
    ) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        return mapper.toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listByOrderIntentId(UUID orderIntentId) {
        return notificationRepository.findByOrderIntentIdOrderByCreatedAtDesc(orderIntentId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listByShipmentId(UUID shipmentId) {
        return notificationRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listByAudience(NotificationAudience audience) {
        return notificationRepository.findByAudienceOrderByCreatedAtDesc(audience)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

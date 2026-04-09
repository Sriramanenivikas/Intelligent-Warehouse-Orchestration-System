package com.iwos.promiseallocation.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.promiseallocation.api.http.PromiseEvaluationResponse;
import com.iwos.promiseallocation.api.http.PromiseItemDecisionResponse;
import com.iwos.promiseallocation.infrastructure.persistence.entity.PromiseEvaluationEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PromiseEvaluationResponseMapper {

    private final ObjectMapper objectMapper;

    public PromiseEvaluationResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PromiseEvaluationResponse toResponse(PromiseEvaluationEntity entity) {
        try {
            List<PromiseItemDecisionResponse> items = objectMapper.readValue(
                    entity.getItemDecisionsJson(),
                    new TypeReference<List<PromiseItemDecisionResponse>>() {
                    }
            );

            return new PromiseEvaluationResponse(
                    entity.getEvaluationId(),
                    entity.getStatus(),
                    entity.getFulfillmentNodeId(),
                    entity.getReason(),
                    entity.getPromisedBy(),
                    entity.getEvaluatedAt(),
                    entity.getCustomerId(),
                    items
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize promise evaluation payload", exception);
        }
    }
}

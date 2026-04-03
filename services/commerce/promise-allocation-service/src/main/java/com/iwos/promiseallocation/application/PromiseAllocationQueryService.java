package com.iwos.promiseallocation.application;

import com.iwos.promiseallocation.api.http.PromiseEvaluationResponse;
import com.iwos.promiseallocation.domain.promise.PromiseEvaluationNotFoundException;
import com.iwos.promiseallocation.infrastructure.persistence.PromiseEvaluationResponseMapper;
import com.iwos.promiseallocation.infrastructure.persistence.repository.PromiseEvaluationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PromiseAllocationQueryService {

    private final PromiseEvaluationRepository promiseEvaluationRepository;
    private final PromiseEvaluationResponseMapper responseMapper;

    public PromiseAllocationQueryService(
            PromiseEvaluationRepository promiseEvaluationRepository,
            PromiseEvaluationResponseMapper responseMapper
    ) {
        this.promiseEvaluationRepository = promiseEvaluationRepository;
        this.responseMapper = responseMapper;
    }

    public PromiseEvaluationResponse getEvaluation(UUID evaluationId) {
        return promiseEvaluationRepository.findById(evaluationId)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new PromiseEvaluationNotFoundException(evaluationId));
    }
}

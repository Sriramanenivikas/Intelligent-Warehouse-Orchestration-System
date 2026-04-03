package com.iwos.promiseallocation.domain.promise;

import java.util.UUID;

public class PromiseEvaluationNotFoundException extends RuntimeException {

    public PromiseEvaluationNotFoundException(UUID evaluationId) {
        super("Promise evaluation not found for evaluationId=%s".formatted(evaluationId));
    }
}

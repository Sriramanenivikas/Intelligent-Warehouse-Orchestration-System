package com.iwos.promiseallocation.api.http;

import com.iwos.promiseallocation.application.PromiseAllocationQueryService;
import com.iwos.promiseallocation.application.PromiseAllocationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/promises")
public class PromiseAllocationController {

    private final PromiseAllocationService promiseAllocationService;
    private final PromiseAllocationQueryService promiseAllocationQueryService;

    public PromiseAllocationController(
            PromiseAllocationService promiseAllocationService,
            PromiseAllocationQueryService promiseAllocationQueryService
    ) {
        this.promiseAllocationService = promiseAllocationService;
        this.promiseAllocationQueryService = promiseAllocationQueryService;
    }

    @PostMapping("/resolve")
    public PromiseEvaluationResponse resolvePromise(@Valid @RequestBody ResolvePromiseRequest request) {
        return promiseAllocationService.resolve(request);
    }

    @GetMapping("/{evaluationId}")
    public PromiseEvaluationResponse getEvaluation(@PathVariable("evaluationId") UUID evaluationId) {
        return promiseAllocationQueryService.getEvaluation(evaluationId);
    }
}

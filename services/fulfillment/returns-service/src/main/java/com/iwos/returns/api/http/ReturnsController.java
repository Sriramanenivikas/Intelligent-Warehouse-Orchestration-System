package com.iwos.returns.api.http;

import com.iwos.returns.application.ReturnsCommandService;
import com.iwos.returns.application.ReturnsQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnsController {

    private final ReturnsCommandService returnsCommandService;
    private final ReturnsQueryService returnsQueryService;

    public ReturnsController(
            ReturnsCommandService returnsCommandService,
            ReturnsQueryService returnsQueryService
    ) {
        this.returnsCommandService = returnsCommandService;
        this.returnsQueryService = returnsQueryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse createReturnRequest(@Valid @RequestBody CreateReturnRequest request) {
        return returnsCommandService.createReturnRequest(request);
    }

    @GetMapping
    public List<ReturnResponse> listReturns(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId
    ) {
        return returnsQueryService.listReturns(status, customerId);
    }

    @GetMapping("/{returnRequestId}")
    public ReturnResponse getReturnRequest(@PathVariable UUID returnRequestId) {
        return returnsQueryService.getReturnRequest(returnRequestId);
    }

    @PostMapping("/{returnRequestId}/approve")
    public ReturnResponse approveReturnRequest(@PathVariable UUID returnRequestId) {
        return returnsCommandService.approveReturnRequest(returnRequestId);
    }

    @PostMapping("/{returnRequestId}/receive")
    public ReturnResponse markReceived(@PathVariable UUID returnRequestId) {
        return returnsCommandService.markReceived(returnRequestId);
    }
}

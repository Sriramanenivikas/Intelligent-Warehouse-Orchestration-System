package com.iwos.taskexecution.api.http;

import com.iwos.taskexecution.application.TaskExecutionService;
import com.iwos.taskexecution.application.TaskIngestionService;
import com.iwos.taskexecution.application.TaskQueryService;
import com.iwos.taskexecution.domain.task.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskQueryService queryService;
    private final TaskExecutionService executionService;
    private final TaskIngestionService ingestionService;

    public TaskController(
            TaskQueryService queryService,
            TaskExecutionService executionService,
            TaskIngestionService ingestionService
    ) {
        this.queryService = queryService;
        this.executionService = executionService;
        this.ingestionService = ingestionService;
    }

    @GetMapping("/{taskAssignmentId}")
    public TaskResponse getById(@PathVariable("taskAssignmentId") UUID taskAssignmentId) {
        return queryService.getById(taskAssignmentId);
    }

    @GetMapping("/by-fulfillment-task/{fulfillmentTaskId}")
    public TaskResponse getByFulfillmentTaskId(@PathVariable("fulfillmentTaskId") UUID fulfillmentTaskId) {
        return queryService.getByFulfillmentTaskId(fulfillmentTaskId);
    }

    @GetMapping
    public List<TaskResponse> listTasks(
            @RequestParam(name = "status", defaultValue = "READY") TaskStatus status,
            @RequestParam(name = "nodeId", required = false) @Nullable String nodeId,
            @RequestParam(name = "limit", defaultValue = "25") @Min(1) @Max(100) int limit
    ) {
        return queryService.listTasks(status, nodeId, limit);
    }

    @GetMapping("/by-fulfillment-order/{fulfillmentOrderId}")
    public List<TaskResponse> listByFulfillmentOrder(@PathVariable("fulfillmentOrderId") UUID fulfillmentOrderId) {
        return queryService.listByFulfillmentOrderId(fulfillmentOrderId);
    }

    @PostMapping("/{taskAssignmentId}/claim")
    public TaskResponse claimTask(
            @PathVariable("taskAssignmentId") UUID taskAssignmentId,
            @Valid @RequestBody ClaimTaskRequest request
    ) {
        return executionService.claimTask(taskAssignmentId, request.workerId());
    }

    @PostMapping("/{taskAssignmentId}/start")
    public TaskResponse startTask(
            @PathVariable("taskAssignmentId") UUID taskAssignmentId,
            @Valid @RequestBody ClaimTaskRequest request
    ) {
        return executionService.startTask(taskAssignmentId, request.workerId());
    }

    @PostMapping("/{taskAssignmentId}/complete")
    public TaskResponse completeTask(
            @PathVariable("taskAssignmentId") UUID taskAssignmentId,
            @Valid @RequestBody CompleteTaskRequest request
    ) {
        return executionService.completeTask(taskAssignmentId, request.workerId(), request.notes());
    }

    @PostMapping("/{taskAssignmentId}/fail")
    public TaskResponse failTask(
            @PathVariable("taskAssignmentId") UUID taskAssignmentId,
            @Valid @RequestBody FailTaskRequest request
    ) {
        return executionService.failTask(taskAssignmentId, request.workerId(), request.reason());
    }

    @PostMapping("/ingest/fulfillment-order/{fulfillmentOrderId}")
    public ResponseEntity<Map<String, Object>> ingestByFulfillmentOrderId(
            @PathVariable("fulfillmentOrderId") UUID fulfillmentOrderId
    ) {
        int count = ingestionService.ingestTasksForFulfillmentOrder(fulfillmentOrderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "fulfillmentOrderId", fulfillmentOrderId,
                "tasksIngested", count
        ));
    }

    @PostMapping("/ingest/order-intent/{orderIntentId}")
    public ResponseEntity<Map<String, Object>> ingestByOrderIntentId(
            @PathVariable("orderIntentId") UUID orderIntentId
    ) {
        int count = ingestionService.ingestTasksForOrderIntent(orderIntentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "orderIntentId", orderIntentId,
                "tasksIngested", count
        ));
    }
}

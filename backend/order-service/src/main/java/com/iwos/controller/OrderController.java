package com.iwos.controller;

import com.iwos.cqrs.command.CreateOrderCommand;
import com.iwos.cqrs.command.CreateOrderCommandHandler;
import com.iwos.dto.CreateOrderRequest;
import com.iwos.dto.OrderResponse;
import com.iwos.entity.Order;
import com.iwos.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ORDER SERVICE REST CONTROLLER
 *
 * Handles all order-related HTTP endpoints
 * Implements CQRS pattern for order creation
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final CreateOrderCommandHandler commandHandler;

    /**
     * Create new order - Uses CQRS Command pattern
     *
     * Flow:
     * 1. Validate request
     * 2. Find optimal warehouse based on location
     * 3. Check inventory availability
     * 4. Create order in PostgreSQL (write model)
     * 5. Publish event to Kafka
     * 6. Store event in Event Store
     * 7. Update MongoDB read model (eventual consistency)
     *
     * @param request CreateOrderRequest with customer, items, delivery info
     * @return OrderResponse with order ID, warehouse, ETA
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("📦 Creating order for customer: {}, items: {}, pincode: {}",
                request.getCustomerId(),
                request.getItems().size(),
                request.getDeliveryAddress().getPincode());

        try {
            // Convert DTO to CQRS Command
            CreateOrderCommand command = CreateOrderCommand.builder()
                    .customerId(request.getCustomerId())
                    .customerName(request.getCustomerName())
                    .customerEmail(request.getCustomerEmail())
                    .customerPhone(request.getCustomerPhone())
                    .items(request.getItems().stream()
                            .map(item -> CreateOrderCommand.OrderItemDTO.builder()
                                    .sku(item.getSku())
                                    .quantity(item.getQuantity())
                                    .unitPrice(item.getUnitPrice())
                                    .build())
                            .collect(Collectors.toList()))
                    .deliveryAddress(CreateOrderCommand.DeliveryAddressDTO.builder()
                            .line1(request.getDeliveryAddress().getLine1())
                            .line2(request.getDeliveryAddress().getLine2())
                            .city(request.getDeliveryAddress().getCity())
                            .state(request.getDeliveryAddress().getState())
                            .pincode(request.getDeliveryAddress().getPincode())
                            .latitude(request.getDeliveryAddress().getLatitude())
                            .longitude(request.getDeliveryAddress().getLongitude())
                            .build())
                    .deliveryType(request.getDeliveryType())
                    .paymentMethod(request.getPaymentMethod())
                    .build();

            // Execute command (CQRS pattern)
            String orderId = commandHandler.handle(command);

            // Fetch created order
            Order order = orderService.getOrderById(orderId);

            // Convert to response DTO
            OrderResponse response = mapToResponse(order);

            log.info("✅ Order created successfully: {}, warehouse: {}, distance: {}km",
                    orderId,
                    order.getWarehouseId(),
                    order.getDistanceKm());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid order request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Failed to create order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("🔍 Fetching order: {}", orderId);

        Order order = orderService.getOrderById(orderId);
        OrderResponse response = mapToResponse(order);

        return ResponseEntity.ok(response);
    }

    /**
     * List all orders with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId) {

        log.info("📋 Listing orders - status: {}, customerId: {}", status, customerId);

        List<Order> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else if (customerId != null) {
            orders = orderService.getOrdersByCustomerId(customerId);
        } else {
            orders = orderService.getAllOrders();
        }

        List<OrderResponse> response = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable String orderId,
            @RequestParam String status) {

        log.info("🔄 Updating order {} status to: {}", orderId, status);

        orderService.updateOrderStatus(orderId, status);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("status", status);
        response.put("message", "Status updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String orderId) {
        log.info("🚫 Cancelling order: {}", orderId);

        orderService.cancelOrder(orderId);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("status", "CANCELLED");
        response.put("message", "Order cancelled successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get pending orders
     */
    @GetMapping("/pending")
    public ResponseEntity<List<OrderResponse>> getPendingOrders() {
        log.info("⏳ Fetching pending orders");

        List<Order> orders = orderService.getOrdersByStatus("PENDING");
        List<OrderResponse> response = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "order-service");
        return ResponseEntity.ok(response);
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .status(order.getStatus())
                .warehouseId(order.getWarehouseId())
                .warehouseName(order.getWarehouseName())
                .totalAmount(order.getTotalAmount())
                .deliveryType(order.getDeliveryType())
                .paymentMethod(order.getPaymentMethod())
                .deliveryAddress(OrderResponse.DeliveryAddressDTO.builder()
                        .line1(order.getDeliveryLine1())
                        .line2(order.getDeliveryLine2())
                        .city(order.getDeliveryCity())
                        .state(order.getDeliveryState())
                        .pincode(order.getDeliveryPincode())
                        .latitude(order.getDeliveryLatitude())
                        .longitude(order.getDeliveryLongitude())
                        .build())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemDTO.builder()
                                .sku(item.getSku())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .distanceKm(order.getDistanceKm())
                .estimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes())
                .build();
    }
}

package com.iwos.orderintake;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Order Intake API.
 * Tests the complete order acceptance flow with idempotency.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:55432/iwos_test",
    "spring.kafka.bootstrap-servers=localhost:9092"
})
@DisplayName("Order Intake API Integration Tests")
class OrderIntakeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String orderId;
    private String customerId;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID().toString();
        customerId = "cust-" + UUID.randomUUID().toString().substring(0, 8);
        idempotencyKey = "order-key-" + System.currentTimeMillis();
    }

    @Test
    @DisplayName("Should accept valid order and return 202 ACCEPTED")
    void testCreateOrderSuccess() throws Exception {
        String createOrderJson = """
                {
                    "customerId": "%s",
                    "idempotencyKey": "%s",
                    "items": [
                        {
                            "sku": "SKU001",
                            "quantity": 2,
                            "pricePerUnit": 100.50
                        }
                    ],
                    "shippingAddress": {
                        "street": "123 Main St",
                        "city": "Mumbai",
                        "state": "MH",
                        "pincode": "400001",
                        "country": "IN"
                    },
                    "paymentMethod": "CREDIT_CARD"
                }
                """.formatted(customerId, idempotencyKey);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.customerId").value(customerId));
    }

    @Test
    @DisplayName("Should reject order with missing required fields")
    void testCreateOrderMissingFields() throws Exception {
        String invalidOrderJson = """
                {
                    "customerId": "%s"
                }
                """.formatted(customerId);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOrderJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Should be idempotent - same key returns same order")
    void testIdempotency() throws Exception {
        String createOrderJson = """
                {
                    "customerId": "%s",
                    "idempotencyKey": "%s",
                    "items": [
                        {
                            "sku": "SKU001",
                            "quantity": 1,
                            "pricePerUnit": 50.00
                        }
                    ],
                    "shippingAddress": {
                        "street": "123 Main St",
                        "city": "Mumbai",
                        "state": "MH",
                        "pincode": "400001",
                        "country": "IN"
                    },
                    "paymentMethod": "CREDIT_CARD"
                }
                """.formatted(customerId, idempotencyKey);

        // First request
        var firstResponse = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderJson))
                .andExpect(status().isAccepted())
                .andReturn();

        String firstOrderId = objectMapper.readTree(firstResponse.getResponse().getContentAsString())
                .get("orderId").asText();

        // Second request with same idempotency key
        var secondResponse = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderJson))
                .andExpect(status().isAccepted())
                .andReturn();

        String secondOrderId = objectMapper.readTree(secondResponse.getResponse().getContentAsString())
                .get("orderId").asText();

        // Both should return the same order ID
        assert firstOrderId.equals(secondOrderId) : "Idempotency failed: orders should be identical";
    }

    @Test
    @DisplayName("Should retrieve order by ID")
    void testGetOrderById() throws Exception {
        // First create an order
        String createOrderJson = """
                {
                    "customerId": "%s",
                    "idempotencyKey": "%s",
                    "items": [
                        {
                            "sku": "SKU002",
                            "quantity": 1,
                            "pricePerUnit": 75.00
                        }
                    ],
                    "shippingAddress": {
                        "street": "456 Oak Ave",
                        "city": "Bangalore",
                        "state": "KA",
                        "pincode": "560001",
                        "country": "IN"
                    },
                    "paymentMethod": "UPI"
                }
                """.formatted(customerId, idempotencyKey);

        var createResponse = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderJson))
                .andExpect(status().isAccepted())
                .andReturn();

        String createdOrderId = objectMapper.readTree(createResponse.getResponse().getContentAsString())
                .get("orderId").asText();

        // Then retrieve it
        mockMvc.perform(get("/api/v1/orders/{orderId}", createdOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(createdOrderId))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent order")
    void testGetOrderNotFound() throws Exception {
        String nonExistentOrderId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/v1/orders/{orderId}", nonExistentOrderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Should list orders with pagination")
    void testListOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    @DisplayName("Should filter orders by status")
    void testListOrdersByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                .param("status", "CREATED")
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should validate shipping address")
    void testCreateOrderInvalidAddress() throws Exception {
        String invalidAddressJson = """
                {
                    "customerId": "%s",
                    "idempotencyKey": "%s",
                    "items": [
                        {
                            "sku": "SKU001",
                            "quantity": 1,
                            "pricePerUnit": 50.00
                        }
                    ],
                    "shippingAddress": {
                        "street": "123 Main St"
                    },
                    "paymentMethod": "CREDIT_CARD"
                }
                """.formatted(customerId, idempotencyKey);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidAddressJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate minimum order amount")
    void testCreateOrderZeroQuantity() throws Exception {
        String invalidQuantityJson = """
                {
                    "customerId": "%s",
                    "idempotencyKey": "%s",
                    "items": [
                        {
                            "sku": "SKU001",
                            "quantity": 0,
                            "pricePerUnit": 50.00
                        }
                    ],
                    "shippingAddress": {
                        "street": "123 Main St",
                        "city": "Mumbai",
                        "state": "MH",
                        "pincode": "400001",
                        "country": "IN"
                    },
                    "paymentMethod": "CREDIT_CARD"
                }
                """.formatted(customerId, idempotencyKey);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidQuantityJson))
                .andExpect(status().isBadRequest());
    }
}

package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Email(message = "Invalid email format")
    private String customerEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String customerPhone;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemDTO> items;

    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDTO deliveryAddress;

    @NotBlank(message = "Delivery type is required")
    @Pattern(regexp = "EXPRESS|STANDARD", message = "Delivery type must be EXPRESS or STANDARD")
    private String deliveryType;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        @NotBlank(message = "SKU is required")
        private String sku;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be positive")
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressDTO {
        @NotBlank(message = "Address line 1 is required")
        private String line1;

        private String line2;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode format")
        private String pincode;

        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;
    }
}

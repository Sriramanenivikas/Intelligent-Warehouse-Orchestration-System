package com.iwos.loadtest.service;

import com.iwos.loadtest.dto.OrderRequest;
import com.iwos.loadtest.model.Pincode;
import com.iwos.loadtest.repository.PincodeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ORDER GENERATOR SERVICE
 *
 * Generates realistic orders at configurable rate (default 10K/sec)
 * Uses reactive WebClient for non-blocking HTTP calls
 * Tracks success/failure metrics for Prometheus
 */
@Service
@Slf4j
public class OrderGeneratorService {

    private final PincodeRepository pincodeRepository;
    private final WebClient webClient;
    private final Random random = new Random();

    @Value("${loadtest.orders-per-second:10000}")
    private int ordersPerSecond;

    @Value("${loadtest.enabled:false}")
    private boolean loadTestEnabled;

    @Value("${loadtest.burst-size:100}")
    private int burstSize;

    // Metrics
    private final Counter ordersGeneratedCounter;
    private final Counter ordersSuccessCounter;
    private final Counter ordersFailureCounter;
    private final Timer orderResponseTimer;
    private final AtomicLong currentRate = new AtomicLong(0);

    // Realistic data
    private static final String[] FIRST_NAMES = {
        "Raj", "Priya", "Amit", "Sneha", "Vikram", "Anjali", "Arjun",
        "Kavya", "Rohan", "Neha", "Siddharth", "Pooja", "Karan", "Aditi"
    };

    private static final String[] LAST_NAMES = {
        "Sharma", "Verma", "Singh", "Patel", "Kumar", "Reddy",
        "Gupta", "Iyer", "Nair", "Rao", "Joshi", "Mehta"
    };

    private static final String[] SKUS = {
        "SKU-001", "SKU-002", "SKU-003", "SKU-004", "SKU-005",
        "SKU-006", "SKU-007", "SKU-008", "SKU-009", "SKU-010",
        "SKU-011", "SKU-012", "SKU-013", "SKU-014", "SKU-015"
    };

    private static final String[] DELIVERY_TYPES = {"EXPRESS", "STANDARD"};

    public OrderGeneratorService(
            PincodeRepository pincodeRepository,
            WebClient.Builder webClientBuilder,
            MeterRegistry meterRegistry,
            @Value("${kong.gateway.url:http://localhost:8000}") String kongUrl,
            @Value("${kong.gateway.jwt-token}") String jwtToken) {

        this.pincodeRepository = pincodeRepository;
        this.webClient = webClientBuilder
                .baseUrl(kongUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .build();

        // Initialize metrics
        this.ordersGeneratedCounter = Counter.builder("orders.generated.total")
                .description("Total orders generated")
                .register(meterRegistry);

        this.ordersSuccessCounter = Counter.builder("orders.success.total")
                .description("Total successful orders")
                .register(meterRegistry);

        this.ordersFailureCounter = Counter.builder("orders.failure.total")
                .description("Total failed orders")
                .register(meterRegistry);

        this.orderResponseTimer = Timer.builder("orders.response.time")
                .description("Order creation response time")
                .register(meterRegistry);
    }

    /**
     * Generate orders at configured rate
     * Runs every 100ms and generates batch of orders
     */
    @Scheduled(fixedDelay = 100)
    public void generateOrderBatch() {
        if (!loadTestEnabled) {
            return;
        }

        // Calculate how many orders to generate in this batch
        int batchSize = Math.min(ordersPerSecond / 10, burstSize);

        log.info("Generating batch of {} orders (target: {}/sec)", batchSize, ordersPerSecond);

        Flux.range(0, batchSize)
                .flatMap(i -> generateAndSendOrder())
                .subscribe(
                    success -> {
                        ordersSuccessCounter.increment();
                        currentRate.incrementAndGet();
                    },
                    error -> {
                        ordersFailureCounter.increment();
                        log.error("Order generation failed: {}", error.getMessage());
                    }
                );
    }

    /**
     * Reset rate counter every second for accurate rate calculation
     */
    @Scheduled(fixedRate = 1000)
    public void resetRateCounter() {
        long rate = currentRate.getAndSet(0);
        log.info("📊 Current order rate: {} orders/sec", rate);
    }

    /**
     * Generate and send a single order
     */
    private Mono<String> generateAndSendOrder() {
        ordersGeneratedCounter.increment();

        OrderRequest order = generateRealisticOrder();

        return orderResponseTimer.record(() ->
                webClient.post()
                        .uri("/api/v1/orders")
                        .header("X-Request-Id", UUID.randomUUID().toString())
                        .bodyValue(order)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(5))
                        .doOnSuccess(response ->
                            log.debug("✅ Order created: {}", response)
                        )
                        .doOnError(error ->
                            log.error("❌ Order failed: {}", error.getMessage())
                        )
        );
    }

    /**
     * Generate realistic order with Indian pincode and geolocation
     */
    private OrderRequest generateRealisticOrder() {
        Pincode pincode = pincodeRepository.findRandomPincode();

        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String customerId = "CUST-" + random.nextInt(100000);

        // Generate 1-5 items
        int itemCount = random.nextInt(5) + 1;
        List<OrderRequest.OrderItemDTO> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < itemCount; i++) {
            String sku = SKUS[random.nextInt(SKUS.length)];
            int quantity = random.nextInt(5) + 1;
            BigDecimal unitPrice = BigDecimal.valueOf(random.nextDouble() * 100 + 10)
                    .setScale(2, RoundingMode.HALF_UP);

            items.add(OrderRequest.OrderItemDTO.builder()
                    .sku(sku)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .build());

            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        // Delivery address with geolocation
        OrderRequest.DeliveryAddressDTO address = OrderRequest.DeliveryAddressDTO.builder()
                .line1((random.nextInt(999) + 1) + ", " + (random.nextBoolean() ? "Street" : "Road") + " " + (random.nextInt(50) + 1))
                .line2("Sector " + (random.nextInt(100) + 1))
                .city(pincode.getCity())
                .state(pincode.getState())
                .pincode(pincode.getPincode())
                .latitude(pincode.getLatitude() + (random.nextDouble() * 0.02 - 0.01))
                .longitude(pincode.getLongitude() + (random.nextDouble() * 0.02 - 0.01))
                .build();

        return OrderRequest.builder()
                .customerId(customerId)
                .customerName(firstName + " " + lastName)
                .customerEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@email.com")
                .customerPhone("+91-" + (9000000000L + random.nextLong() % 1000000000L))
                .items(items)
                .deliveryAddress(address)
                .deliveryType(DELIVERY_TYPES[random.nextInt(DELIVERY_TYPES.length)])
                .paymentMethod("ONLINE")
                .totalAmount(totalAmount)
                .build();
    }
}

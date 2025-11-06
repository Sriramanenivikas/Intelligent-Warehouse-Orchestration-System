package com.iwos.service;

import com.iwos.dto.CreateWarehouseRequest;
import com.iwos.dto.UpdateWarehouseRequest;
import com.iwos.entity.Warehouse;
import com.iwos.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Warehouse Service
 * Handles warehouse business logic and data operations
 *
 * SOLID Principles:
 * - Single Responsibility: Handles Warehouse business logic
 * - Dependency Inversion: Depends on repository abstraction
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarehouseService {

    private final WarehouseRepository repository;

    /**
     * Get all Warehouses
     */
    @Transactional(readOnly = true)
    public List<Warehouse> getAll() {
        log.info("Fetching all Warehouses");
        return repository.findAll();
    }

    /**
     * Get all active warehouses
     */
    @Transactional(readOnly = true)
    public List<Warehouse> getAllActive() {
        log.info("Fetching all active Warehouses");
        return repository.findByIsActiveTrue();
    }

    /**
     * Get Warehouse by ID
     */
    @Transactional(readOnly = true)
    public Optional<Warehouse> getById(Long id) {
        log.info("Fetching Warehouse with ID: {}", id);
        return repository.findById(id);
    }

    /**
     * Get Warehouse by code
     */
    @Transactional(readOnly = true)
    public Optional<Warehouse> getByCode(String code) {
        log.info("Fetching Warehouse with code: {}", code);
        return repository.findByCode(code);
    }

    /**
     * Create Warehouse from request DTO
     */
    public Warehouse create(CreateWarehouseRequest request) {
        log.info("Creating new Warehouse with code: {}", request.getCode());

        // Check if warehouse with code already exists
        if (repository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Warehouse with code " + request.getCode() + " already exists");
        }

        Warehouse warehouse = Warehouse.builder()
            .code(request.getCode())
            .name(request.getName())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .country(request.getCountry())
            .postalCode(request.getPostalCode())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .capacitySqm(request.getCapacitySqm())
            .maxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : 10000)
            .currentLoad(0)
            .priority(request.getPriority() != null ? request.getPriority() : 5)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .inventory(request.getInventory() != null ? request.getInventory() : new HashMap<>())
            .managerName(request.getManagerName())
            .contactPhone(request.getContactPhone())
            .contactEmail(request.getContactEmail())
            .operatingHours(request.getOperatingHours())
            .build();

        Warehouse saved = repository.save(warehouse);
        log.info("Successfully created Warehouse with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update Warehouse
     */
    public Warehouse update(Long id, UpdateWarehouseRequest request) {
        log.info("Updating Warehouse with ID: {}", id);

        Warehouse warehouse = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with ID: " + id));

        // Update only non-null fields
        if (request.getName() != null) {
            warehouse.setName(request.getName());
        }
        if (request.getAddress() != null) {
            warehouse.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            warehouse.setCity(request.getCity());
        }
        if (request.getState() != null) {
            warehouse.setState(request.getState());
        }
        if (request.getCountry() != null) {
            warehouse.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            warehouse.setPostalCode(request.getPostalCode());
        }
        if (request.getLatitude() != null) {
            warehouse.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            warehouse.setLongitude(request.getLongitude());
        }
        if (request.getCapacitySqm() != null) {
            warehouse.setCapacitySqm(request.getCapacitySqm());
        }
        if (request.getMaxCapacity() != null) {
            warehouse.setMaxCapacity(request.getMaxCapacity());
        }
        if (request.getCurrentLoad() != null) {
            warehouse.setCurrentLoad(request.getCurrentLoad());
        }
        if (request.getPriority() != null) {
            warehouse.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            warehouse.setIsActive(request.getIsActive());
        }
        if (request.getManagerName() != null) {
            warehouse.setManagerName(request.getManagerName());
        }
        if (request.getContactPhone() != null) {
            warehouse.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            warehouse.setContactEmail(request.getContactEmail());
        }
        if (request.getOperatingHours() != null) {
            warehouse.setOperatingHours(request.getOperatingHours());
        }

        Warehouse updated = repository.save(warehouse);
        log.info("Successfully updated Warehouse with ID: {}", id);
        return updated;
    }

    /**
     * Delete Warehouse
     */
    public void delete(Long id) {
        log.info("Deleting Warehouse with ID: {}", id);
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Warehouse not found with ID: " + id);
        }
        repository.deleteById(id);
        log.info("Successfully deleted Warehouse with ID: {}", id);
    }

    /**
     * Update warehouse inventory
     */
    public Warehouse updateInventory(Long id, String sku, int quantity) {
        log.info("Updating inventory for Warehouse ID: {}, SKU: {}, Quantity: {}", id, sku, quantity);

        Warehouse warehouse = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with ID: " + id));

        if (quantity > 0) {
            warehouse.addInventory(sku, quantity);
        } else {
            warehouse.removeInventory(sku, Math.abs(quantity));
        }

        return repository.save(warehouse);
    }

    /**
     * Get warehouses by city
     */
    @Transactional(readOnly = true)
    public List<Warehouse> getByCity(String city) {
        log.info("Fetching warehouses in city: {}", city);
        return repository.findByCityIgnoreCase(city);
    }

    /**
     * Get warehouses with available capacity
     */
    @Transactional(readOnly = true)
    public List<Warehouse> getWarehousesWithAvailableCapacity() {
        log.info("Fetching warehouses with available capacity");
        return repository.findWarehousesWithAvailableCapacity();
    }

    /**
     * Count active warehouses
     */
    @Transactional(readOnly = true)
    public Long countActive() {
        return repository.countActiveWarehouses();
    }
}

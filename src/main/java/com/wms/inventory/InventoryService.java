package com.wms.inventory;

import com.wms.common.ResourceNotFoundException;
import com.wms.product.Product;
import com.wms.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final StockLocationRepository locationRepo;
    private final StockItemRepository stockItemRepo;
    private final StockItemTrackingRepository trackingRepo;
    private final ProductRepository productRepo;

    public InventoryService(StockLocationRepository locationRepo, StockItemRepository stockItemRepo,
                            StockItemTrackingRepository trackingRepo, ProductRepository productRepo) {
        this.locationRepo = locationRepo;
        this.stockItemRepo = stockItemRepo;
        this.trackingRepo = trackingRepo;
        this.productRepo = productRepo;
    }

    // ── Stock Locations ──

    public List<StockLocation> findAllLocations() { return locationRepo.findAll(); }
    public List<StockLocation> findRootLocations() { return locationRepo.findByParentIsNull(); }
    public List<StockLocation> findChildLocations(Long parentId) { return locationRepo.findByParentId(parentId); }

    public StockLocation findLocationById(Long id) {
        return locationRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("StockLocation", id));
    }

    public StockLocation createLocation(LocationDto dto) {
        StockLocation loc = new StockLocation();
        loc.setName(dto.name());
        loc.setDescription(dto.description());
        if (dto.parentId() != null) {
            loc.setParent(findLocationById(dto.parentId()));
        }
        return locationRepo.save(loc);
    }

    public StockLocation updateLocation(Long id, LocationDto dto) {
        StockLocation loc = findLocationById(id);
        loc.setName(dto.name());
        loc.setDescription(dto.description());
        if (dto.parentId() != null) {
            loc.setParent(findLocationById(dto.parentId()));
        } else {
            loc.setParent(null);
        }
        return locationRepo.save(loc);
    }

    public void deleteLocation(Long id) { locationRepo.deleteById(id); }

    // ── Stock Items ──

    public List<StockItem> findAllStockItems() { return stockItemRepo.findAll(); }

    public List<StockItem> findStockItems(Long productId, Long locationId, StockItem.Status status) {
        if (productId != null && status != null) return stockItemRepo.findByProductIdAndStatus(productId, status);
        if (productId != null) return stockItemRepo.findByProductId(productId);
        if (locationId != null) return stockItemRepo.findByLocationId(locationId);
        if (status != null) return stockItemRepo.findByStatus(status);
        return stockItemRepo.findAll();
    }

    public StockItem findStockItemById(Long id) {
        return stockItemRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("StockItem", id));
    }

    public StockItem createStockItem(StockItemDto dto) {
        Product product = productRepo.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", dto.productId()));
        StockItem item = new StockItem();
        item.setProduct(product);
        item.setQuantity(dto.quantity() != null ? dto.quantity() : BigDecimal.ZERO);
        item.setPurchasePrice(dto.purchasePrice());
        item.setBatch(dto.batch());
        item.setSerialNumber(dto.serialNumber());
        item.setExpiryDate(dto.expiryDate());
        item.setProductionDate(dto.productionDate());
        if (dto.locationId() != null) {
            item.setLocation(findLocationById(dto.locationId()));
        }
        item = stockItemRepo.save(item);
        addTracking(item, StockItemTracking.TrackingType.RECEIVED, item.getQuantity(), "Initial stock entry");
        return item;
    }

    public StockItem adjustStock(Long id, AdjustDto dto) {
        StockItem item = findStockItemById(id);
        BigDecimal oldQty = item.getQuantity();
        item.setQuantity(dto.newQuantity());
        if (dto.newQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            item.setStatus(StockItem.Status.DEPLETED);
        }
        item = stockItemRepo.save(item);
        BigDecimal delta = dto.newQuantity().subtract(oldQty);
        addTracking(item, StockItemTracking.TrackingType.ADJUSTED, delta, dto.notes());
        return item;
    }

    public StockItem moveStock(Long id, MoveDto dto) {
        StockItem item = findStockItemById(id);
        StockLocation newLocation = findLocationById(dto.newLocationId());
        item.setLocation(newLocation);
        item = stockItemRepo.save(item);
        addTracking(item, StockItemTracking.TrackingType.MOVED, BigDecimal.ZERO,
                "Moved to " + newLocation.getName() + (dto.notes() != null ? " — " + dto.notes() : ""));
        return item;
    }

    public List<StockItemTracking> getTrackingHistory(Long stockItemId) {
        return trackingRepo.findByStockItemIdOrderByCreatedAtDesc(stockItemId);
    }

    private void addTracking(StockItem item, StockItemTracking.TrackingType type, BigDecimal delta, String notes) {
        StockItemTracking t = new StockItemTracking();
        t.setStockItem(item);
        t.setTrackingType(type);
        t.setQuantityDelta(delta);
        t.setNotes(notes);
        trackingRepo.save(t);
    }

    // ── DTOs ──

    public record LocationDto(String name, String description, Long parentId) {}
    public record StockItemDto(Long productId, Long locationId, BigDecimal quantity, BigDecimal purchasePrice,
                               String batch, String serialNumber, LocalDate expiryDate, LocalDate productionDate) {}
    public record AdjustDto(BigDecimal newQuantity, String notes) {}
    public record MoveDto(Long newLocationId, String notes) {}
}

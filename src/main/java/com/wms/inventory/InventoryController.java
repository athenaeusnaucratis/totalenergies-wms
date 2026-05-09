package com.wms.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    // ── Stock Locations ──

    @GetMapping("/stock-locations")
    public List<StockLocation> listLocations() { return service.findAllLocations(); }

    @GetMapping("/stock-locations/roots")
    public List<StockLocation> rootLocations() { return service.findRootLocations(); }

    @GetMapping("/stock-locations/{id}")
    public StockLocation getLocation(@PathVariable Long id) { return service.findLocationById(id); }

    @PostMapping("/stock-locations")
    public ResponseEntity<StockLocation> createLocation(@RequestBody InventoryService.LocationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLocation(dto));
    }

    @PutMapping("/stock-locations/{id}")
    public StockLocation updateLocation(@PathVariable Long id, @RequestBody InventoryService.LocationDto dto) {
        return service.updateLocation(id, dto);
    }

    @DeleteMapping("/stock-locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        service.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    // ── Stock Items ──

    @GetMapping("/stock-items")
    public List<StockItem> listStockItems(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) StockItem.Status status) {
        return service.findStockItems(productId, locationId, status);
    }

    @GetMapping("/stock-items/{id}")
    public StockItem getStockItem(@PathVariable Long id) { return service.findStockItemById(id); }

    @PostMapping("/stock-items")
    public ResponseEntity<StockItem> createStockItem(@RequestBody InventoryService.StockItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createStockItem(dto));
    }

    @PostMapping("/stock-items/{id}/adjust")
    public StockItem adjustStock(@PathVariable Long id, @RequestBody InventoryService.AdjustDto dto) {
        return service.adjustStock(id, dto);
    }

    @PostMapping("/stock-items/{id}/move")
    public StockItem moveStock(@PathVariable Long id, @RequestBody InventoryService.MoveDto dto) {
        return service.moveStock(id, dto);
    }

    @GetMapping("/stock-items/{id}/tracking")
    public List<StockItemTracking> getTracking(@PathVariable Long id) {
        return service.getTrackingHistory(id);
    }
}

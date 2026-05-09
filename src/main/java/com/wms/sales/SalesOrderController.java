package com.wms.sales;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService service;

    public SalesOrderController(SalesOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<SalesOrder> list(@RequestParam(required = false) SalesOrder.Status status) {
        if (status != null) return service.findByStatus(status);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SalesOrder get(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<SalesOrder> create(@RequestBody SalesOrderService.SalesOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/{id}/confirm")
    public SalesOrder confirm(@PathVariable Long id) { return service.confirm(id); }

    @PostMapping("/{id}/allocate")
    public SalesOrder allocate(@PathVariable Long id, @RequestBody List<SalesOrderService.AllocationDto> allocations) {
        return service.allocate(id, allocations);
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<Shipment> ship(@PathVariable Long id, @RequestBody SalesOrderService.ShipDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ship(id, dto));
    }

    @PostMapping("/{id}/complete")
    public SalesOrder complete(@PathVariable Long id) { return service.complete(id); }

    @PostMapping("/{id}/cancel")
    public SalesOrder cancel(@PathVariable Long id) { return service.cancel(id); }
}

package com.wms.purchasing;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<PurchaseOrder> list(@RequestParam(required = false) PurchaseOrder.Status status) {
        if (status != null) return service.findByStatus(status);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseOrder get(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<PurchaseOrder> create(@RequestBody PurchaseOrderService.PurchaseOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PostMapping("/{id}/place")
    public PurchaseOrder place(@PathVariable Long id) { return service.place(id); }

    @PostMapping("/{id}/cancel")
    public PurchaseOrder cancel(@PathVariable Long id) { return service.cancel(id); }

    @PostMapping("/{id}/receive")
    public ResponseEntity<GoodsReceipt> receive(@PathVariable Long id,
                                                 @RequestBody PurchaseOrderService.ReceiveGoodsDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.receiveGoods(id, dto));
    }
}

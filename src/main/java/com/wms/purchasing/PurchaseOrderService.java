package com.wms.purchasing;

import com.wms.accounting.CostingService;
import com.wms.accounting.JournalService;
import com.wms.common.ResourceNotFoundException;
import com.wms.inventory.StockItem;
import com.wms.inventory.StockItemRepository;
import com.wms.inventory.StockItemTracking;
import com.wms.inventory.StockItemTrackingRepository;
import com.wms.inventory.StockLocationRepository;
import com.wms.product.Company;
import com.wms.product.CompanyRepository;
import com.wms.product.Product;
import com.wms.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderLineRepository poLineRepo;
    private final GoodsReceiptRepository grRepo;
    private final GoodsReceiptLineRepository grLineRepo;
    private final StockItemRepository stockItemRepo;
    private final StockItemTrackingRepository trackingRepo;
    private final StockLocationRepository locationRepo;
    private final CompanyRepository companyRepo;
    private final ProductRepository productRepo;
    private final CostingService costingService;
    private final JournalService journalService;

    public PurchaseOrderService(PurchaseOrderRepository poRepo, PurchaseOrderLineRepository poLineRepo,
                                GoodsReceiptRepository grRepo, GoodsReceiptLineRepository grLineRepo,
                                StockItemRepository stockItemRepo, StockItemTrackingRepository trackingRepo,
                                StockLocationRepository locationRepo, CompanyRepository companyRepo,
                                ProductRepository productRepo, CostingService costingService,
                                JournalService journalService) {
        this.poRepo = poRepo;
        this.poLineRepo = poLineRepo;
        this.grRepo = grRepo;
        this.grLineRepo = grLineRepo;
        this.stockItemRepo = stockItemRepo;
        this.trackingRepo = trackingRepo;
        this.locationRepo = locationRepo;
        this.companyRepo = companyRepo;
        this.productRepo = productRepo;
        this.costingService = costingService;
        this.journalService = journalService;
    }

    public List<PurchaseOrder> findAll() { return poRepo.findAllByOrderByCreatedAtDesc(); }
    public List<PurchaseOrder> findByStatus(PurchaseOrder.Status status) { return poRepo.findByStatus(status); }

    public PurchaseOrder findById(Long id) {
        return poRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    public PurchaseOrder create(PurchaseOrderDto dto) {
        Company supplier = companyRepo.findById(dto.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", dto.supplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(dto.orderDate() != null ? dto.orderDate() : LocalDate.now());
        po.setExpectedDate(dto.expectedDate());
        po.setNotes(dto.notes());

        for (PurchaseOrderLineDto lineDto : dto.lines()) {
            Product product = productRepo.findById(lineDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", lineDto.productId()));
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setPurchaseOrder(po);
            line.setProduct(product);
            line.setQuantity(lineDto.quantity());
            line.setUnitPrice(lineDto.unitPrice());
            po.getLines().add(line);
        }
        return poRepo.save(po);
    }

    public PurchaseOrder place(Long id) {
        PurchaseOrder po = findById(id);
        if (po.getStatus() != PurchaseOrder.Status.PENDING) {
            throw new IllegalStateException("PO must be in PENDING status to place");
        }
        po.setStatus(PurchaseOrder.Status.PLACED);
        return poRepo.save(po);
    }

    public PurchaseOrder cancel(Long id) {
        PurchaseOrder po = findById(id);
        if (po.getStatus() == PurchaseOrder.Status.COMPLETE) {
            throw new IllegalStateException("Cannot cancel a completed PO");
        }
        po.setStatus(PurchaseOrder.Status.CANCELLED);
        return poRepo.save(po);
    }

    public GoodsReceipt receiveGoods(Long poId, ReceiveGoodsDto dto) {
        PurchaseOrder po = findById(poId);
        if (po.getStatus() != PurchaseOrder.Status.PLACED) {
            throw new IllegalStateException("PO must be PLACED to receive goods");
        }

        GoodsReceipt gr = new GoodsReceipt();
        gr.setPurchaseOrder(po);
        gr.setReceiptDate(dto.receiptDate() != null ? dto.receiptDate() : LocalDate.now());
        gr.setNotes(dto.notes());
        gr = grRepo.save(gr);

        boolean allReceived = true;
        for (ReceiveLineDto lineDto : dto.lines()) {
            PurchaseOrderLine poLine = poLineRepo.findById(lineDto.poLineId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderLine", lineDto.poLineId()));

            StockItem stockItem = new StockItem();
            stockItem.setProduct(poLine.getProduct());
            stockItem.setQuantity(lineDto.quantityReceived());
            stockItem.setPurchasePrice(poLine.getUnitPrice());
            if (lineDto.locationId() != null) {
                stockItem.setLocation(locationRepo.findById(lineDto.locationId()).orElse(null));
            }
            stockItem = stockItemRepo.save(stockItem);

            StockItemTracking tracking = new StockItemTracking();
            tracking.setStockItem(stockItem);
            tracking.setTrackingType(StockItemTracking.TrackingType.RECEIVED);
            tracking.setQuantityDelta(lineDto.quantityReceived());
            tracking.setNotes("Received from PO #" + poId);
            trackingRepo.save(tracking);

            GoodsReceiptLine grLine = new GoodsReceiptLine();
            grLine.setGoodsReceipt(gr);
            grLine.setPoLineId(poLine.getId());
            grLine.setStockItem(stockItem);
            grLine.setQuantityReceived(lineDto.quantityReceived());
            grLine.setLocationId(lineDto.locationId());
            grLineRepo.save(grLine);

            poLine.setReceivedQty(poLine.getReceivedQty().add(lineDto.quantityReceived()));
            poLineRepo.save(poLine);

            BigDecimal amount = lineDto.quantityReceived().multiply(poLine.getUnitPrice());
            costingService.updateCostOnReceipt(poLine.getProduct().getId(), lineDto.quantityReceived(), poLine.getUnitPrice());
            journalService.createGoodsReceiptJournal(poId, poLine.getProduct().getId(), amount);

            if (poLine.getReceivedQty().compareTo(poLine.getQuantity()) < 0) {
                allReceived = false;
            }
        }

        if (allReceived) {
            boolean fullyReceived = po.getLines().stream()
                    .allMatch(l -> l.getReceivedQty().compareTo(l.getQuantity()) >= 0);
            if (fullyReceived) {
                po.setStatus(PurchaseOrder.Status.COMPLETE);
                poRepo.save(po);
            }
        }
        return gr;
    }

    public record PurchaseOrderLineDto(Long productId, BigDecimal quantity, BigDecimal unitPrice) {}
    public record PurchaseOrderDto(Long supplierId, LocalDate orderDate, LocalDate expectedDate,
                                   String notes, List<PurchaseOrderLineDto> lines) {}
    public record ReceiveLineDto(Long poLineId, BigDecimal quantityReceived, Long locationId) {}
    public record ReceiveGoodsDto(LocalDate receiptDate, String notes, List<ReceiveLineDto> lines) {}
}

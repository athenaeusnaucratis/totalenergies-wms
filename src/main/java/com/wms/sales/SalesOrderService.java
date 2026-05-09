package com.wms.sales;

import com.wms.accounting.*;
import com.wms.common.ResourceNotFoundException;
import com.wms.inventory.StockItem;
import com.wms.inventory.StockItemRepository;
import com.wms.inventory.StockItemTracking;
import com.wms.inventory.StockItemTrackingRepository;
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
public class SalesOrderService {

    private final SalesOrderRepository soRepo;
    private final SalesOrderLineRepository soLineRepo;
    private final SalesOrderAllocationRepository allocRepo;
    private final ShipmentRepository shipmentRepo;
    private final ShipmentItemRepository shipmentItemRepo;
    private final StockItemRepository stockItemRepo;
    private final StockItemTrackingRepository trackingRepo;
    private final CompanyRepository companyRepo;
    private final ProductRepository productRepo;
    private final CostingService costingService;
    private final JournalService journalService;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceItemRepository invoiceItemRepo;

    public SalesOrderService(SalesOrderRepository soRepo, SalesOrderLineRepository soLineRepo,
                             SalesOrderAllocationRepository allocRepo, ShipmentRepository shipmentRepo,
                             ShipmentItemRepository shipmentItemRepo, StockItemRepository stockItemRepo,
                             StockItemTrackingRepository trackingRepo, CompanyRepository companyRepo,
                             ProductRepository productRepo, CostingService costingService,
                             JournalService journalService, InvoiceRepository invoiceRepo,
                             InvoiceItemRepository invoiceItemRepo) {
        this.soRepo = soRepo;
        this.soLineRepo = soLineRepo;
        this.allocRepo = allocRepo;
        this.shipmentRepo = shipmentRepo;
        this.shipmentItemRepo = shipmentItemRepo;
        this.stockItemRepo = stockItemRepo;
        this.trackingRepo = trackingRepo;
        this.companyRepo = companyRepo;
        this.productRepo = productRepo;
        this.costingService = costingService;
        this.journalService = journalService;
        this.invoiceRepo = invoiceRepo;
        this.invoiceItemRepo = invoiceItemRepo;
    }

    public List<SalesOrder> findAll() { return soRepo.findAllByOrderByCreatedAtDesc(); }
    public List<SalesOrder> findByStatus(SalesOrder.Status status) { return soRepo.findByStatus(status); }

    public SalesOrder findById(Long id) {
        return soRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
    }

    public SalesOrder create(SalesOrderDto dto) {
        Company customer = companyRepo.findById(dto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", dto.customerId()));
        SalesOrder so = new SalesOrder();
        so.setCustomer(customer);
        so.setOrderDate(dto.orderDate() != null ? dto.orderDate() : LocalDate.now());
        so.setNotes(dto.notes());

        for (SalesOrderLineDto lineDto : dto.lines()) {
            Product product = productRepo.findById(lineDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", lineDto.productId()));
            SalesOrderLine line = new SalesOrderLine();
            line.setSalesOrder(so);
            line.setProduct(product);
            line.setQuantity(lineDto.quantity());
            line.setUnitPrice(lineDto.unitPrice());
            so.getLines().add(line);
        }
        return soRepo.save(so);
    }

    public SalesOrder confirm(Long id) {
        SalesOrder so = findById(id);
        if (so.getStatus() != SalesOrder.Status.PENDING) throw new IllegalStateException("SO must be PENDING");
        so.setStatus(SalesOrder.Status.CONFIRMED);
        return soRepo.save(so);
    }

    public SalesOrder allocate(Long soId, List<AllocationDto> allocations) {
        SalesOrder so = findById(soId);
        if (so.getStatus() != SalesOrder.Status.CONFIRMED && so.getStatus() != SalesOrder.Status.ALLOCATED) {
            throw new IllegalStateException("SO must be CONFIRMED or ALLOCATED");
        }

        for (AllocationDto alloc : allocations) {
            SalesOrderLine line = soLineRepo.findById(alloc.soLineId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalesOrderLine", alloc.soLineId()));
            StockItem stockItem = stockItemRepo.findById(alloc.stockItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("StockItem", alloc.stockItemId()));

            if (stockItem.getQuantity().compareTo(alloc.quantity()) < 0) {
                throw new IllegalStateException("Insufficient stock for item #" + alloc.stockItemId());
            }

            SalesOrderAllocation allocation = new SalesOrderAllocation();
            allocation.setSoLineId(line.getId());
            allocation.setStockItem(stockItem);
            allocation.setQuantity(alloc.quantity());
            allocRepo.save(allocation);

            stockItem.setQuantity(stockItem.getQuantity().subtract(alloc.quantity()));
            stockItem.setStatus(StockItem.Status.ALLOCATED);
            stockItemRepo.save(stockItem);

            StockItemTracking tracking = new StockItemTracking();
            tracking.setStockItem(stockItem);
            tracking.setTrackingType(StockItemTracking.TrackingType.ALLOCATED);
            tracking.setQuantityDelta(alloc.quantity().negate());
            tracking.setNotes("Allocated to SO #" + soId);
            trackingRepo.save(tracking);

            line.setAllocatedQty(line.getAllocatedQty().add(alloc.quantity()));
            soLineRepo.save(line);
        }
        so.setStatus(SalesOrder.Status.ALLOCATED);
        return soRepo.save(so);
    }

    public Shipment ship(Long soId, ShipDto dto) {
        SalesOrder so = findById(soId);
        if (so.getStatus() != SalesOrder.Status.ALLOCATED) {
            throw new IllegalStateException("SO must be ALLOCATED to ship");
        }

        Shipment shipment = new Shipment();
        shipment.setSalesOrder(so);
        shipment.setShipmentDate(dto.shipmentDate() != null ? dto.shipmentDate() : LocalDate.now());
        shipment.setTrackingNumber(dto.trackingNumber());
        shipment.setNotes(dto.notes());
        shipment = shipmentRepo.save(shipment);

        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (SalesOrderLine line : so.getLines()) {
            List<SalesOrderAllocation> allocs = allocRepo.findBySoLineId(line.getId());
            for (SalesOrderAllocation alloc : allocs) {
                StockItem stockItem = alloc.getStockItem();
                stockItem.setStatus(StockItem.Status.SHIPPED);
                stockItemRepo.save(stockItem);

                StockItemTracking tracking = new StockItemTracking();
                tracking.setStockItem(stockItem);
                tracking.setTrackingType(StockItemTracking.TrackingType.SHIPPED);
                tracking.setQuantityDelta(BigDecimal.ZERO);
                tracking.setNotes("Shipped — SO #" + soId);
                trackingRepo.save(tracking);

                ShipmentItem si = new ShipmentItem();
                si.setShipment(shipment);
                si.setSoLineId(line.getId());
                si.setStockItem(stockItem);
                si.setQuantity(alloc.getQuantity());
                shipmentItemRepo.save(si);

                BigDecimal cogs = costingService.getCOGS(line.getProduct().getId(), alloc.getQuantity());
                journalService.createShipmentJournal(soId, line.getProduct().getId(), cogs);

                line.setShippedQty(line.getShippedQty().add(alloc.getQuantity()));
            }
            totalRevenue = totalRevenue.add(line.getShippedQty().multiply(line.getUnitPrice()));
            soLineRepo.save(line);
        }

        journalService.createSalesInvoiceJournal(soId, totalRevenue);

        Invoice invoice = new Invoice();
        invoice.setInvoiceType(Invoice.InvoiceType.SALES);
        invoice.setCompany(so.getCustomer());
        invoice.setOrderId(soId);
        invoice.setTotalAmount(totalRevenue);
        invoice = invoiceRepo.save(invoice);

        for (SalesOrderLine line : so.getLines()) {
            InvoiceItem ii = new InvoiceItem();
            ii.setInvoice(invoice);
            ii.setProductId(line.getProduct().getId());
            ii.setQuantity(line.getShippedQty());
            ii.setUnitPrice(line.getUnitPrice());
            ii.setAmount(line.getShippedQty().multiply(line.getUnitPrice()));
            invoiceItemRepo.save(ii);
        }

        so.setStatus(SalesOrder.Status.SHIPPED);
        soRepo.save(so);
        return shipment;
    }

    public SalesOrder complete(Long id) {
        SalesOrder so = findById(id);
        if (so.getStatus() != SalesOrder.Status.SHIPPED) throw new IllegalStateException("SO must be SHIPPED");
        so.setStatus(SalesOrder.Status.COMPLETE);
        return soRepo.save(so);
    }

    public SalesOrder cancel(Long id) {
        SalesOrder so = findById(id);
        if (so.getStatus() == SalesOrder.Status.SHIPPED || so.getStatus() == SalesOrder.Status.COMPLETE) {
            throw new IllegalStateException("Cannot cancel shipped/completed SO");
        }
        so.setStatus(SalesOrder.Status.CANCELLED);
        return soRepo.save(so);
    }

    public record SalesOrderLineDto(Long productId, BigDecimal quantity, BigDecimal unitPrice) {}
    public record SalesOrderDto(Long customerId, LocalDate orderDate, String notes, List<SalesOrderLineDto> lines) {}
    public record AllocationDto(Long soLineId, Long stockItemId, BigDecimal quantity) {}
    public record ShipDto(LocalDate shipmentDate, String trackingNumber, String notes) {}
}

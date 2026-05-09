package com.wms.accounting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class CostingService {

    private final ProductCostingRepository costingRepo;

    public CostingService(ProductCostingRepository costingRepo) {
        this.costingRepo = costingRepo;
    }

    public ProductCosting updateCostOnReceipt(Long productId, BigDecimal receivedQty, BigDecimal unitPrice) {
        ProductCosting costing = costingRepo.findByProductId(productId).orElseGet(() -> {
            ProductCosting c = new ProductCosting();
            c.setProductId(productId);
            return c;
        });

        BigDecimal oldTotal = costing.getTotalValue();
        BigDecimal oldQty = costing.getTotalQty();
        BigDecimal newTotal = oldTotal.add(receivedQty.multiply(unitPrice));
        BigDecimal newQty = oldQty.add(receivedQty);

        costing.setTotalValue(newTotal);
        costing.setTotalQty(newQty);
        if (newQty.compareTo(BigDecimal.ZERO) > 0) {
            costing.setAvgUnitCost(newTotal.divide(newQty, 4, RoundingMode.HALF_UP));
        }
        return costingRepo.save(costing);
    }

    public BigDecimal getCOGS(Long productId, BigDecimal shippedQty) {
        ProductCosting costing = costingRepo.findByProductId(productId).orElse(null);
        if (costing == null || costing.getAvgUnitCost().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal cogs = shippedQty.multiply(costing.getAvgUnitCost()).setScale(2, RoundingMode.HALF_UP);

        costing.setTotalQty(costing.getTotalQty().subtract(shippedQty));
        costing.setTotalValue(costing.getTotalValue().subtract(cogs));
        costingRepo.save(costing);
        return cogs;
    }
}

package com.wms.accounting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class JournalService {

    private final AcctgTransRepository transRepo;
    private final GlAccountRepository glAccountRepo;

    public JournalService(AcctgTransRepository transRepo, GlAccountRepository glAccountRepo) {
        this.transRepo = transRepo;
        this.glAccountRepo = glAccountRepo;
    }

    public AcctgTrans createGoodsReceiptJournal(Long poId, Long productId, BigDecimal amount) {
        GlAccount inventory = glAccountRepo.findByAccountCode("1000").orElseThrow();
        GlAccount ap = glAccountRepo.findByAccountCode("2000").orElseThrow();

        AcctgTrans trans = new AcctgTrans();
        trans.setTransDate(LocalDate.now());
        trans.setDescription("Goods Receipt — PO #" + poId);
        trans.setSourceType("GOODS_RECEIPT");
        trans.setSourceId(poId);
        trans.setPosted(true);

        AcctgTransEntry drInventory = new AcctgTransEntry();
        drInventory.setAcctgTrans(trans);
        drInventory.setGlAccount(inventory);
        drInventory.setDebitAmount(amount);
        drInventory.setProductId(productId);
        drInventory.setDescription("DR Inventory");

        AcctgTransEntry crAP = new AcctgTransEntry();
        crAP.setAcctgTrans(trans);
        crAP.setGlAccount(ap);
        crAP.setCreditAmount(amount);
        crAP.setProductId(productId);
        crAP.setDescription("CR Accounts Payable");

        trans.getEntries().add(drInventory);
        trans.getEntries().add(crAP);
        return transRepo.save(trans);
    }

    public AcctgTrans createShipmentJournal(Long soId, Long productId, BigDecimal cogsAmount) {
        GlAccount cogs = glAccountRepo.findByAccountCode("5000").orElseThrow();
        GlAccount inventory = glAccountRepo.findByAccountCode("1000").orElseThrow();

        AcctgTrans trans = new AcctgTrans();
        trans.setTransDate(LocalDate.now());
        trans.setDescription("COGS — SO #" + soId);
        trans.setSourceType("SHIPMENT");
        trans.setSourceId(soId);
        trans.setPosted(true);

        AcctgTransEntry drCogs = new AcctgTransEntry();
        drCogs.setAcctgTrans(trans);
        drCogs.setGlAccount(cogs);
        drCogs.setDebitAmount(cogsAmount);
        drCogs.setProductId(productId);
        drCogs.setDescription("DR COGS");

        AcctgTransEntry crInventory = new AcctgTransEntry();
        crInventory.setAcctgTrans(trans);
        crInventory.setGlAccount(inventory);
        crInventory.setCreditAmount(cogsAmount);
        crInventory.setProductId(productId);
        crInventory.setDescription("CR Inventory");

        trans.getEntries().add(drCogs);
        trans.getEntries().add(crInventory);
        return transRepo.save(trans);
    }

    public AcctgTrans createSalesInvoiceJournal(Long soId, BigDecimal revenueAmount) {
        GlAccount ar = glAccountRepo.findByAccountCode("1100").orElseThrow();
        GlAccount revenue = glAccountRepo.findByAccountCode("4000").orElseThrow();

        AcctgTrans trans = new AcctgTrans();
        trans.setTransDate(LocalDate.now());
        trans.setDescription("Sales Invoice — SO #" + soId);
        trans.setSourceType("INVOICE");
        trans.setSourceId(soId);
        trans.setPosted(true);

        AcctgTransEntry drAR = new AcctgTransEntry();
        drAR.setAcctgTrans(trans);
        drAR.setGlAccount(ar);
        drAR.setDebitAmount(revenueAmount);
        drAR.setDescription("DR Accounts Receivable");

        AcctgTransEntry crRevenue = new AcctgTransEntry();
        crRevenue.setAcctgTrans(trans);
        crRevenue.setGlAccount(revenue);
        crRevenue.setCreditAmount(revenueAmount);
        crRevenue.setDescription("CR Revenue");

        trans.getEntries().add(drAR);
        trans.getEntries().add(crRevenue);
        return transRepo.save(trans);
    }
}

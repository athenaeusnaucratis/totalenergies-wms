package com.wms.accounting;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AcctgTransRepository extends JpaRepository<AcctgTrans, Long> {
    List<AcctgTrans> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
    List<AcctgTrans> findAllByOrderByCreatedAtDesc();
}

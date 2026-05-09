package com.wms.accounting;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GlAccountRepository extends JpaRepository<GlAccount, Long> {
    Optional<GlAccount> findByAccountCode(String accountCode);
}

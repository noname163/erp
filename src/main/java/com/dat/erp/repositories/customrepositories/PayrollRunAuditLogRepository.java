package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;


import com.dat.erp.entities.PayrollRunAuditLog;


public interface PayrollRunAuditLogRepository extends JpaRepository<PayrollRunAuditLog, Long> {
}

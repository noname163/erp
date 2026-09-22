package com.dat.erp.entities;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.dat.erp.utils.UuidV7;

@Entity
@Table(name = "employee_payroll_input_version", indexes = @Index(name = "idx_payroll_input_employee_period", columnList = "company_code,employee_code,effective_from"))
@Getter
@NoArgsConstructor
public class EmployeePayrollInputVersion extends BaseAuditableEntity {
    @Column(name = "employee_code", nullable = false) private String employeeCode;
    @Column(name = "effective_from", nullable = false) private LocalDate effectiveFrom;
    @Column(name = "encrypted_inputs", columnDefinition = "text", nullable = false) private String encryptedInputs;
    public EmployeePayrollInputVersion(String employeeCode, LocalDate effectiveFrom, String encryptedInputs) {
        assignCode("PIV" + UuidV7.generate());
        this.employeeCode = employeeCode;
        this.effectiveFrom = effectiveFrom;
        this.encryptedInputs = encryptedInputs;
    }
}

package com.dat.erp.services.payroll;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dat.erp.data.EmployeePayrollInputs;
import com.dat.erp.entities.EmployeePayrollInputVersion;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.repositories.customrepositories.EmployeePayrollInputVersionRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import com.dat.erp.utils.PayslipJson;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollInputService {
    private final EmployeePayrollInputVersionRepository repository;
    private final SecurityContextService security;
    public EmployeePayrollInputVersion find(String company, String employee, LocalDate date) {
        return repository.findFirstByCompanyCodeAndEmployeeCodeAndEffectiveFromLessThanEqualAndIsDeletedFalseOrderByEffectiveFromDescIdDesc(company, employee, date).orElse(null);
    }
    public EmployeePayrollInputs decode(EmployeePayrollInputVersion version) {
        return PayslipJson.read(CompanySecretKeyCryptoUtils.decrypt(version.getEncryptedInputs(), security.getCurrentCompanySecretKey()), EmployeePayrollInputs.class);
    }
    @Transactional
    public void save(String employee, LocalDate from, EmployeePayrollInputs inputs) {
        if (from.getDayOfMonth() != 1) throw new BadRequestException("Payroll inputs must take effect on the first day of a month");
        if (inputs.getOtherDeduction().signum() > 0 && blank(inputs.getDeductionReason()))
            throw new BadRequestException("Enter a reason for the other deduction");
        if ((!inputs.getSocialInsurance() || !inputs.getHealthInsurance() || !inputs.getUnemploymentInsurance()) && blank(inputs.getInsuranceExemptionReason()))
            throw new BadRequestException("Explain the employee's insurance exemption or ineligibility");
        repository.save(new EmployeePayrollInputVersion(employee, from,
            CompanySecretKeyCryptoUtils.encrypt(PayslipJson.write(inputs), security.getCurrentCompanySecretKey())));
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}

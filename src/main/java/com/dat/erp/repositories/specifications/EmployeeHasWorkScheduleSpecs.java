package com.dat.erp.repositories.specifications;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.WorkSchedule;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class EmployeeHasWorkScheduleSpecs {

    private EmployeeHasWorkScheduleSpecs() {
    }

    public static Specification<EmployeeHasWorkSchedule> byDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null)
                return cb.conjunction();

            Join<EmployeeHasWorkSchedule, WorkSchedule> ws = root.join("workSchedule", JoinType.INNER);
            if (startDate != null && endDate != null) {
                return cb.between(ws.get("shiftDate"), startDate, endDate);
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(ws.get("shiftDate"), startDate);
            } else {
                return cb.lessThanOrEqualTo(ws.get("shiftDate"), endDate);
            }
        };
    }

    // Adjust the path below if companyCode lives elsewhere (e.g.,
    // employee.company.code)
    public static Specification<EmployeeHasWorkSchedule> byCompanyCode(String companyCode) {
        return (root, query, cb) -> {
            if (companyCode == null || companyCode.isBlank())
                return cb.conjunction();
            Join<EmployeeHasWorkSchedule, EmployeeInformation> emp = root.join("employee", JoinType.INNER);
            return cb.equal(
                    cb.lower(emp.get("companyCode")),
                    companyCode.toLowerCase(Locale.ROOT));
        };
    }

    // Works for String-based shiftType; if it's an enum, parse to enum before
    // building the spec
    public static Specification<EmployeeHasWorkSchedule> byShiftType(String shiftType) {
        return (root, query, cb) -> {
            if (shiftType == null || shiftType.isBlank())
                return cb.conjunction();
            Join<EmployeeHasWorkSchedule, WorkSchedule> ws = root.join("workSchedule", JoinType.INNER);
            return cb.equal(
                    cb.lower(ws.get("shiftType")),
                    shiftType.toLowerCase(Locale.ROOT));
        };
    }

    /** Convenience combiner */
    public static Specification<EmployeeHasWorkSchedule> filter(LocalDate startDate,
            LocalDate endDate,
            String companyCode,
            String shiftType) {
        return Specification.allOf(
                byDateRange(startDate, endDate),
                byCompanyCode(companyCode),
                byShiftType(shiftType));
    }
}
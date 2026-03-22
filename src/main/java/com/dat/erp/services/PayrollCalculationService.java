package com.dat.erp.services;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeePto;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.EmploymentAgreement;
import com.dat.erp.entities.PayRateRule;
import com.dat.erp.entities.PayrollAdjustment;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.entities.WorkScheduleDetail;

public interface PayrollCalculationService {
    PayrollEmployeeDraft calculate(
            UserProfile userProfile,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<EmploymentAgreement> agreements,
            List<EmployeeSalary> salaryRecords,
            List<EmployeeSalaryDetail> salaryDetails,
            List<PayrollPolicy> policies,
            List<PayRateRule> payRateRules,
            WorkSchedule workSchedule,
            List<WorkScheduleDetail> scheduleDetails,
            CompanyCalendar calendar,
            List<com.dat.erp.entities.CalendarDate> calendarDates,
            List<DailyWork> dailyWorks,
            List<EmployeePto> ptoRecords,
            List<PayrollAdjustment> adjustments);
}

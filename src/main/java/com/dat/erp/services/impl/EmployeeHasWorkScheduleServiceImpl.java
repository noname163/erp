package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.ListCodeTypeEnum;
import com.dat.erp.constants.ShiftType;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.mapper.interfaces.EmployeeHasWorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.EmployeeHasWorkScheduleRepository;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.services.EmployeeHasWorkScheduleService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

@Service
public class EmployeeHasWorkScheduleServiceImpl implements EmployeeHasWorkScheduleService {
    @Autowired
    private EmployeeHasWorkScheduleRepository employeeHasWorkScheduleRepository;
    @Autowired
    private EmployeeHasWorkScheduleMapper employeeHasWorkScheduleMapper;
    @Autowired
    private WorkScheduleRepository workScheduleRepository;
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private SecurityContextService securityContextService;

    @Override
    public String createEmployeeHasWorkSchedule(EmployeeHasWorkScheduleRequest request) {
        List<EmployeeHasWorkSchedule> employeeHasWorkSchedules = new ArrayList<>();
        if (request.getType() == ListCodeTypeEnum.EMPLOYEE) {
            List<EmployeeInformation> employeeInformations = employeeInformationRepository
                    .findByCodeIn(request.getCodes());
            WorkSchedule workSchedule = workScheduleRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new RuntimeException("WorkSchedule not found"));

            for (EmployeeInformation employeeInformation : employeeInformations) {
                EmployeeHasWorkSchedule employeeHasWorkSchedule = EmployeeHasWorkSchedule.builder()
                        .employee(employeeInformation)
                        .workSchedule(workSchedule)
                        .build();
                employeeHasWorkSchedules.add(employeeHasWorkSchedule);
            }

        } else {
            List<WorkSchedule> workSchedules = workScheduleRepository.findByCodeIn(
                    request.getCodes());
            EmployeeInformation employeeInformation = employeeInformationRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new RuntimeException("EmployeeInformation not found"));
            for (WorkSchedule workSchedule : workSchedules) {
                EmployeeHasWorkSchedule employeeHasWorkSchedule = EmployeeHasWorkSchedule.builder()
                        .employee(employeeInformation)
                        .workSchedule(workSchedule)
                        .build();
                employeeHasWorkSchedules.add(employeeHasWorkSchedule);
            }
        }
        employeeHasWorkScheduleRepository.saveAll(employeeHasWorkSchedules);
        return "EmployeeHasWorkSchedule created successfully";
    }

    @Override
    public String createEmployeeHasWorkScheduleByDateQuantityAndType(LocalDate shiftDate, ShiftType shiftType,
            Integer quantity) {
        if (shiftDate == null || shiftType == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("shiftDate, shiftType and quantity are required, quantity must be > 0");
        }

        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        WorkSchedule workSchedule = workScheduleRepository
                .findByShiftDateAndShiftTypeAndQuantityAndCompany(shiftDate, shiftType, quantity,
                        currentUser.getCompany())
                .orElseThrow(() -> new RuntimeException("WorkSchedule not found for provided parameters"));

        List<EmployeeHasWorkSchedule> existing = employeeHasWorkScheduleRepository.findByWorkSchedule(workSchedule);
        int remaining = Math.max(0, (workSchedule.getQuantity() != null ? workSchedule.getQuantity() : quantity)
                - existing.size());
        if (remaining <= 0) {
            return "No remaining slots to assign.";
        }

        Set<String> assignedCodes = new HashSet<>();
        for (EmployeeHasWorkSchedule e : existing) {
            if (e.getEmployee() != null && e.getEmployee().getCode() != null) {
                assignedCodes.add(e.getEmployee().getCode());
            }
        }

        List<EmployeeHasWorkSchedule> toSave = new ArrayList<>();
        for (EmployeeInformation emp : employeeInformationRepository.findAll()) {
            if (emp.getCompany() != null && emp.getCompany().getCode().equals(currentUser.getCompany().getCode())
                    && !assignedCodes.contains(emp.getCode())) {
                toSave.add(EmployeeHasWorkSchedule.builder().employee(emp).workSchedule(workSchedule).build());
                if (toSave.size() >= remaining) break;
            }
        }

        if (!toSave.isEmpty()) {
            employeeHasWorkScheduleRepository.saveAll(toSave);
        }
        return "Assigned " + toSave.size() + " employees to the work schedule.";
    }

    @Override
    public PagedResponse<EmployeeHasWorkScheduleResponse> getListWorkScheduleByCode(String code, ListCodeTypeEnum type,
            Integer page, Integer pageSize, String sortBy, String sortDir) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getListWorkScheduleByCode'");
    }

}

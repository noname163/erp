package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    public String createEmployeeHasWorkScheduleByDateQuantityAndType(LocalDate shiftDate, ShiftType shiftType,
            Integer quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'createEmployeeHasWorkScheduleByDateQuantityAndType'");
    }

    @Override
    public PagedResponse<EmployeeHasWorkScheduleResponse> getListWorkScheduleByCode(String code, ListCodeTypeEnum type,
            Integer page, Integer pageSize, String sortBy, String sortDir) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getListWorkScheduleByCode'");
    }

}

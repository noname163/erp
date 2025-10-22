package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.ListCodeTypeEnum;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.searchs.EmployeeScheduleFilter;
import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.EmployeeHasWorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.EmployeeHasWorkScheduleRepository;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.repositories.specifications.EmployeeHasWorkScheduleSpecs;
import com.dat.erp.services.EmployeeHasWorkScheduleService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.PageableUtils;

@Service
public class EmployeeHasWorkScheduleServiceImpl implements EmployeeHasWorkScheduleService {
    @Autowired
    private EmployeeHasWorkScheduleRepository employeeHasWorkScheduleRepository;
    @Autowired
    private WorkScheduleRepository workScheduleRepository;
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private SecurityContextService securityContextService;
    @Autowired
    private EmployeeHasWorkScheduleMapper employeeHasWorkScheduleMapper;

    @Override
    public String createEmployeeHasWorkSchedule(EmployeeHasWorkScheduleRequest request) {
        List<EmployeeHasWorkSchedule> employeeHasWorkSchedules = new ArrayList<>();
        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        if (request.getType() == ListCodeTypeEnum.EMPLOYEE) {
            List<EmployeeInformation> employeeInformations = employeeInformationRepository
                    .findByCodeInAndCompany(request.getCodes(), currentUser.getEmployee().getCompany());
            WorkSchedule workSchedule = workScheduleRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new RuntimeException(Messages.ERROR_WORK_SCHEDULE_NOT_FOUND));

            for (EmployeeInformation employeeInformation : employeeInformations) {
                if (employeeHasWorkScheduleRepository.existsByEmployee_CodeAndWorkSchedule_Code(
                        employeeInformation.getCode(), workSchedule.getCode())) {
                    throw new ConflictException(Messages.ERROR_EMPLOYEE_SCHEDULE_EXISTS);
                }
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
                    .orElseThrow(() -> new RuntimeException(Messages.ERROR_EMPLOYEE_NOT_FOUND));
            for (WorkSchedule workSchedule : workSchedules) {
                if (employeeHasWorkScheduleRepository.existsByEmployee_CodeAndWorkSchedule_Code(
                        employeeInformation.getCode(), workSchedule.getCode())) {
                    throw new ConflictException(Messages.ERROR_EMPLOYEE_SCHEDULE_EXISTS);
                }
                EmployeeHasWorkSchedule employeeHasWorkSchedule = EmployeeHasWorkSchedule.builder()
                        .employee(employeeInformation)
                        .workSchedule(workSchedule)
                        .build();
                employeeHasWorkSchedules.add(employeeHasWorkSchedule);
            }
        }
        employeeHasWorkScheduleRepository.saveAll(employeeHasWorkSchedules);
        return Messages.EMPLOYEE_HAS_WORK_SCHEDULE_CREATED;
    }

    @Override
    public PagedResponse<EmployeeHasWorkScheduleResponse> getEmployeeSchedule(EmployeeScheduleFilter filter,
            Integer pageSize, Integer pageNum, String sortBy, String sortDir) {
        Specification<EmployeeHasWorkSchedule> spec = EmployeeHasWorkScheduleSpecs.filter(
                filter.startDate(), filter.endDate(), filter.companyCode(), filter.shiftType());

        Pageable pageable = PageableUtils.create(pageNum, pageSize, sortBy, sortDir);
        Page<EmployeeHasWorkSchedule> page = employeeHasWorkScheduleRepository.findAll(spec, pageable);

        return PageableUtils.mapPage(page, employeeHasWorkScheduleMapper::toResponse, Messages.SUCCESS);

    }

}

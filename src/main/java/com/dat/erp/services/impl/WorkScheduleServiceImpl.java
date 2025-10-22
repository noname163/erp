package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.ListCodeTypeEnum;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.WorkScheduleResponse;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.WorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.services.EmployeeHasWorkScheduleService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.WorkScheduleService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.PageableUtils;

@Service
public class WorkScheduleServiceImpl implements WorkScheduleService {

    @Autowired
    private WorkScheduleRepository workScheduleRepository;
    @Autowired
    private WorkScheduleMapper workScheduleMapper;
    @Autowired
    private SecurityContextService securityContextService;
    @Autowired
    private EmployeeHasWorkScheduleService employeeHasWorkScheduleService;

    @Override
    public String createWorkScheduleService(WorkScheduleRequest workScheduleRequest) {
        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        workScheduleRepository.findByShiftDateAndShiftTypeAndQuantityAndCompany(
                workScheduleRequest.getShiftDate(),
                workScheduleRequest.getShiftType(),
                workScheduleRequest.getQuantity(),
                currentUser.getEmployee().getCompany())
                .ifPresent(existing -> {
                    throw new ConflictException(Messages.ERROR_WORK_SCHEDULE_EXISTS);
                });
        WorkSchedule workSchedule = workScheduleMapper.toEntity(workScheduleRequest);
        setBasicInformation(workSchedule);
        workScheduleRepository.save(workSchedule);
        return workSchedule.getCode();
    }

    @Override
    public PagedResponse<WorkScheduleResponse> getWorkSchedule(LocalDate shiftDate, Integer pageSize, Integer pageNum,
            String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(pageNum, pageSize, sortBy, sortDir);
        Page<WorkSchedule> pageData = workScheduleRepository.findAll(pageable);

        return PageableUtils.mapPage(pageData, workScheduleMapper::toResponse, Messages.SUCCESS);
    }

    @Override
    public String createWorkSchedulesService(List<WorkScheduleRequest> workScheduleRequests) {
        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        for (WorkScheduleRequest req : workScheduleRequests) {
            workScheduleRepository.findByShiftDateAndShiftTypeAndQuantityAndCompany(
                    req.getShiftDate(), req.getShiftType(), req.getQuantity(), currentUser.getEmployee().getCompany())
                    .ifPresent(existing -> {
                        throw new com.dat.erp.exceptions.ConflictException(Messages.ERROR_WORK_SCHEDULE_EXISTS);
                    });
        }
        List<WorkSchedule> workSchedules = workScheduleMapper.toEntityList(workScheduleRequests);
        for (WorkSchedule workSchedule : workSchedules) {
            setBasicInformation(workSchedule);
        }
        workScheduleRepository.saveAll(workSchedules);
        return Messages.SUCCESS;
    }

    private void setBasicInformation(WorkSchedule workSchedule) {
        CustomUserDetails customUserDetails = securityContextService.getCurrentUser();
        workSchedule.setCode(CodePrefixes.WORK_SCHEDULE + UUID.randomUUID());
        workSchedule.setCompany(customUserDetails.getCompany());
        workSchedule.setStatus(CommonStatus.ACTIVATE);
    }

    @Override
    public String createWorkScheduleWithEmployees(WorkScheduleRequest workScheduleRequest, List<String> employeeCodes) {
        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        WorkSchedule workSchedule = workScheduleRepository
                .findByShiftDateAndShiftTypeAndQuantityAndCompany(workScheduleRequest.getShiftDate(),
                        workScheduleRequest.getShiftType(), workScheduleRequest.getQuantity(),
                        currentUser.getEmployee().getCompany())
                .orElseGet(() -> {
                    WorkSchedule newWorkSchedule = workScheduleMapper.toEntity(workScheduleRequest);
                    setBasicInformation(newWorkSchedule);
                    return workScheduleRepository.save(newWorkSchedule);
                });
        employeeHasWorkScheduleService.createEmployeeHasWorkSchedule(
                EmployeeHasWorkScheduleRequest
                        .builder()
                        .code(workSchedule.getCode())
                        .type(ListCodeTypeEnum.EMPLOYEE)
                        .codes(employeeCodes).build());

        return Messages.WORK_SCHEDULE_CREATED_WITH_EMPLOYEES;
    }

}

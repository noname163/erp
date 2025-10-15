package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.WorkScheduleResponse;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.mapper.interfaces.WorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
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

    @Override
    public String createWorkScheduleService(WorkScheduleRequest workScheduleRequest) {
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

        return PageableUtils.mapPage(pageData, workScheduleMapper::toResponse, "Success");
    }

    @Override
    public String createWorkSchedulesService(List<WorkScheduleRequest> workScheduleRequests) {
        List<WorkSchedule> workSchedules = workScheduleMapper.toEntityList(workScheduleRequests);
        for (WorkSchedule workSchedule : workSchedules) {
            setBasicInformation(workSchedule);
        }
        workScheduleRepository.saveAll(workSchedules);
        return "Success";
    }

    private void setBasicInformation(WorkSchedule workSchedule) {
        CustomUserDetails customUserDetails = securityContextService.getCurrentUser();
        workSchedule.setCode("WRS-" + UUID.randomUUID());
        workSchedule.setCompany(customUserDetails.getCompany());
        workSchedule.setStatus(CommonStatus.ACTIVATE);
    }

}

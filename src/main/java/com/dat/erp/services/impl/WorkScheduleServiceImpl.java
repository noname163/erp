package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.WorkScheduleResponse;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.mapper.interfaces.WorkScheduleMapper;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.services.WorkScheduleService;
import com.dat.erp.utils.PageableUtils;

@Service
public class WorkScheduleServiceImpl implements WorkScheduleService {

    @Autowired
    private WorkScheduleRepository workScheduleRepository;
    @Autowired
    private WorkScheduleMapper workScheduleMapper;

    @Override
    public String createWorkScheduleService(WorkScheduleRequest workScheduleRequest) {
        WorkSchedule workSchedule = workScheduleMapper.toEntity(workScheduleRequest);
        workSchedule.setCode("WRS-" + UUID.randomUUID());
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

}

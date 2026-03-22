package com.dat.erp.services;

import java.util.List;

import com.dat.erp.dto.request.payroll.WorkScheduleRequest;
import com.dat.erp.dto.response.payroll.WorkScheduleResponse;

public interface WorkScheduleService {
    WorkScheduleResponse create(WorkScheduleRequest request);

    WorkScheduleResponse update(String code, WorkScheduleRequest request);

    WorkScheduleResponse get(String code);

    List<WorkScheduleResponse> list();

    void delete(String code);
}

package com.dat.erp.services;

import com.dat.erp.constants.WorkScheduleEnum;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;

public interface EmployeeHasWorkScheduleService {
    public String createEmployeeHasWorkSchedule(EmployeeHasWorkScheduleRequest request);

    public PagedResponse<EmployeeHasWorkScheduleResponse> getListWorkScheduleByCode(String code, WorkScheduleEnum type,
            Integer page, Integer pageSize, String sortBy,
            String sortDir);
}

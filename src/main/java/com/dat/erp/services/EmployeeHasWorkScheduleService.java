package com.dat.erp.services;

import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.searchs.EmployeeScheduleFilter;

public interface EmployeeHasWorkScheduleService {
    public String createEmployeeHasWorkSchedule(EmployeeHasWorkScheduleRequest request);

    public PagedResponse<EmployeeHasWorkScheduleResponse> getEmployeeSchedule(EmployeeScheduleFilter filter,
            Integer pageSize, Integer pageNum,
            String sortBy, String sortDir);

}

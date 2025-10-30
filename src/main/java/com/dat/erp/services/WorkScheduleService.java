package com.dat.erp.services;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.WorkScheduleResponse;

public interface WorkScheduleService {
    public String createWorkScheduleService(WorkScheduleRequest workScheduleRequest);

    public String createWorkSchedulesService(List<WorkScheduleRequest> workScheduleRequests);

    public PagedResponse<WorkScheduleResponse> getWorkSchedule(LocalDate shiftDate, Integer pageSize, Integer pageNum,
            String sortBy, String sortDir);

    public String createWorkScheduleWithEmployees(WorkScheduleRequest workScheduleRequest, List<String> employeeCodes);
}

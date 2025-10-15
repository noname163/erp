package com.dat.erp.services;

import java.time.LocalDate;

import com.dat.erp.constants.ListCodeTypeEnum;
import com.dat.erp.constants.ShiftType;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;

public interface EmployeeHasWorkScheduleService {
    public String createEmployeeHasWorkSchedule(EmployeeHasWorkScheduleRequest request);

    public String createEmployeeHasWorkScheduleByDateQuantityAndType(LocalDate shiftDate, ShiftType shiftType,
            Integer quantity);

    public PagedResponse<EmployeeHasWorkScheduleResponse> getListWorkScheduleByCode(String code, ListCodeTypeEnum type,
            Integer page, Integer pageSize, String sortBy,
            String sortDir);
}

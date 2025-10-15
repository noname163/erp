package com.dat.erp.services.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.dat.erp.constants.ListCodeTypeEnum;
import com.dat.erp.constants.ShiftType;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.services.EmployeeHasWorkScheduleService;

@Service
public class EmployeeHasWorkScheduleServiceImpl implements EmployeeHasWorkScheduleService {

    @Override
    public String createEmployeeHasWorkSchedule(EmployeeHasWorkScheduleRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createEmployeeHasWorkSchedule'");
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

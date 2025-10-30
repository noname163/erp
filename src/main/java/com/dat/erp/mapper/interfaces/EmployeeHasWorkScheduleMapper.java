package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.entities.EmployeeHasWorkSchedule;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface EmployeeHasWorkScheduleMapper {

    // Entity → Response
    @Mapping(source = "employee.code", target = "employeeCode")
    @Mapping(source = "employee.nickname", target = "employeeName")
    @Mapping(source = "workSchedule.code", target = "scheduleCode")
    @Mapping(source = "workSchedule.shiftType", target = "shiftType")
    @Mapping(source = "workSchedule.shiftDate", target = "date")
    EmployeeHasWorkScheduleResponse toResponse(EmployeeHasWorkSchedule entity);
}

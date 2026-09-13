package com.dat.erp.mapper.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.dat.erp.constants.DayType;
import com.dat.erp.dto.response.EmployeeDailyWorkListResponse;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;
import com.dat.erp.repositories.projections.EmployeeDailyWorkListProjection;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface EmployeeDailyWorkMapper {
    @Mapping(target = "employeeName", source = "employeeName", qualifiedByName = "trimToNull")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "toLocalTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "toLocalTime")
    @Mapping(target = "createdBy", source = "createdByName", qualifiedByName = "trimToNull")
    @Mapping(target = "editedBy", source = "editedByName", qualifiedByName = "trimToNull")
    @Mapping(target = "workType", source = "workType", qualifiedByName = "dayTypeToString")
    EmployeeDailyWorkListResponse toListResponse(EmployeeDailyWorkListProjection projection);

    default DailyWorkForSalaryResponse toSalaryResponse(DayType dayType, BigDecimal hoursWorked) {
        return new DailyWorkForSalaryResponse(dayType, toTotalWorkHours(hoursWorked));
    }

    default int toTotalWorkHours(BigDecimal hoursWorked) {
        return hoursWorked == null ? 0 : hoursWorked.intValue();
    }

    @Named("toLocalTime")
    default LocalTime toLocalTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalTime();
    }

    @Named("trimToNull")
    default String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    @Named("dayTypeToString")
    default String dayTypeToString(DayType value) {
        return value == null ? null : value.name();
    }
}

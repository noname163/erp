package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.CompanyCalendarRequest;
import com.dat.erp.dto.response.CompanyCalendarListResponse;
import com.dat.erp.dto.response.CompanyCalendarResponse;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = CalendarDateMapper.class)
public interface CompanyCalendarMapper {
    @Mapping(target = "name", expression = "java(trim(request.getName()))")
    @Mapping(target = "region", expression = "java(trim(request.getRegion()))")
    @Mapping(target = "timeZone", expression = "java(trim(request.getTimeZone()))")
    @Mapping(target = "note", expression = "java(trim(request.getNote()))")
    @Mapping(target = "dates", ignore = true)
    CompanyCalendar toEntity(CompanyCalendarRequest request);

    CompanyCalendarResponse toResponse(CompanyCalendar entity);

    CompanyCalendarListResponse toListResponse(CompanyCalendar entity);

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}

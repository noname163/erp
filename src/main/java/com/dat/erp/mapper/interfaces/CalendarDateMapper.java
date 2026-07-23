package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.CompanyCalendarDateRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface CalendarDateMapper {
    @Mapping(target = "calendar", ignore = true)
    @Mapping(target = "note", expression = "java(request.getNote() == null ? null : request.getNote().trim())")
    CalendarDate toEntity(CompanyCalendarDateRequest request);

    List<CalendarDate> toEntities(List<CompanyCalendarDateRequest> requests);

    CompanyCalendarDateResponse toResponse(CalendarDate entity);
}

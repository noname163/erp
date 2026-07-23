package com.dat.erp.mapper.interfaces;

import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

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

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dates", ignore = true)
    void updateEntity(CompanyCalendarRequest request, @MappingTarget CompanyCalendar entity);

    CompanyCalendarResponse toResponse(CompanyCalendar entity);

    CompanyCalendarListResponse toListResponse(CompanyCalendar entity);

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}

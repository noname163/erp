package com.dat.erp.mapper.interfaces;

import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.dto.response.WorkScheduleResponse;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.systemconfigs.CentralMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface WorkScheduleMapper {

    WorkSchedule toEntity(WorkScheduleRequest request);

    WorkScheduleResponse toResponse(WorkSchedule entity);

    List<WorkScheduleResponse> toResponseList(List<WorkSchedule> entities);

    List<WorkSchedule> toEntityList(List<WorkScheduleRequest> requests);
}

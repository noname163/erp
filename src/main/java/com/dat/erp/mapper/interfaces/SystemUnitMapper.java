package com.dat.erp.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;

import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SystemUnitMapper {
    SelectionOptionResponse toOptionResponse(SystemUnit unit);

    List<SelectionOptionResponse> toOptionResponses(List<SystemUnit> units);
}

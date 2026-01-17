package com.dat.erp.mapper.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.entities.Email;
import com.dat.erp.systemconfigs.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface EmailMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "isSent", ignore = true)
    @Mapping(target = "retryTime", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "needRetry", ignore = true)
    @Mapping(source = "from", target = "emailFrom")
    @Mapping(source = "to", target = "emailTo")
    Email toEntity(EmailRequest request);
}

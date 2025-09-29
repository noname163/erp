package com.dat.erp.dto.response;

import com.dat.erp.constants.ApiType;
import com.dat.erp.constants.CommonEnum;

import lombok.Data;

@Data
public class SystemApiResponse {

    private Long id;

    private String endpoint;

    private String description;

    private ApiType type;

    private CommonEnum systemType;

    private String code;
}

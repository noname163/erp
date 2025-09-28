package com.dat.erp.dto.request;

import com.dat.erp.constants.ApiType;
import com.dat.erp.constants.CommonEnum;

import lombok.Data;

@Data
public class SystemApiRequest {

    private String endpoint;

    private String description;

    private ApiType type; // e.g., PUBLIC, PRIVATE, INTERNAL

    private CommonEnum systemType; // e.g., ERP, CRM, etc.
}

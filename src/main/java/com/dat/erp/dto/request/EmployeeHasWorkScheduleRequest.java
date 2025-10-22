package com.dat.erp.dto.request;

import java.util.List;

import com.dat.erp.constants.ListCodeTypeEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EmployeeHasWorkScheduleRequest {
    private String code;
    private List<String> codes;
    private ListCodeTypeEnum type;
}

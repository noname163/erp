package com.dat.erp.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkScheduleResponse {
    private Long id;
    private LocalDate shiftDate;
    private Integer quantity;
    private String shiftType;
    private String status;
    private String code;
}

package com.dat.erp.dto.request;

import lombok.Data;
import java.time.LocalDate;

import com.dat.erp.constants.ShiftType;

@Data
public class WorkScheduleRequest {
    private LocalDate shiftDate;
    private Integer quantity;
    private ShiftType shiftType;
}

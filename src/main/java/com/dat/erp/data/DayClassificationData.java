package com.dat.erp.data;

import com.dat.erp.constants.DayType;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DayClassificationData {

    private final DayType dayType;
    private final DayType workType;

    public DayClassificationData(
            DayType dayType,
            DayType workType) {

        this.dayType = dayType;
        this.workType = workType;

        if (dayType == null && workType == null) {
            ErrorUtils.requireNonNull(dayType, "Day classification is required");
        }
    }
}

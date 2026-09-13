package com.dat.erp.data;

import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperationalTextData {

    private final String note;
    private final String errorMessage;
    private final String reason;
    private final String formulaNote;

    public OperationalTextData(
            String note,
            String errorMessage,
            String reason,
            String formulaNote) {

        this.note = note == null ? null : ErrorUtils.requireNotBlank(note, "Note must not be blank");
        this.errorMessage = errorMessage == null ? null : ErrorUtils.requireNotBlank(errorMessage, "Error message must not be blank");
        this.reason = reason == null ? null : ErrorUtils.requireNotBlank(reason, "Reason must not be blank");
        this.formulaNote = formulaNote == null ? null : ErrorUtils.requireNotBlank(formulaNote, "Formula note must not be blank");

        if (this.note == null && this.errorMessage == null && this.reason == null && this.formulaNote == null) {
            throw new BadRequestException("At least one operational text value is required");
        }
    }
}

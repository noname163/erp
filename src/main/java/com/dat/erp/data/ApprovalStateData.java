package com.dat.erp.data;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApprovalStateData {

    private final ApprovalStatus approvalStatus;
    private final String status;

    public ApprovalStateData(
            ApprovalStatus approvalStatus,
            String status) {

        this.approvalStatus = approvalStatus;
        this.status = status == null ? null : ErrorUtils.requireNotBlank(status, "Status must not be blank");
    }
}

package com.dat.erp.data;

import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactInfoData {

    private final String email;
    private final String phoneNumber;
    private final String address;

    public ContactInfoData(
            String email,
            String phoneNumber,
            String address) {

        this.email = email == null ? null : requireEmail(email);
        this.phoneNumber = phoneNumber == null ? null : ErrorUtils.requireNotBlank(phoneNumber, "Phone number must not be blank");
        this.address = address == null ? null : ErrorUtils.requireNotBlank(address, "Address must not be blank");

        if (this.email == null && this.phoneNumber == null && this.address == null) {
            throw new BadRequestException("At least one contact value is required");
        }
    }

    private static String requireEmail(
            String value) {

        String normalized = ErrorUtils.requireNotBlank(value, "Email must not be blank");
        if (!normalized.contains("@")) {
            throw new BadRequestException("Email must be valid");
        }
        return normalized;
    }
}

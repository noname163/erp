package com.dat.erp.utils;

import org.apache.commons.lang3.StringUtils;

import com.dat.erp.exceptions.BadRequestException;

public class ErrorUtils {
    public static String requireNotBlank(
            String value,
            String message) {

        if (StringUtils.isBlank(value)) {
            throw new BadRequestException(message);
        }

        return value;
    }

    public static <T> T requireNonNull(
            T value,
            String message) {

        if (value == null) {
            throw new BadRequestException(message);
        }

        return value;
    }

    public static Integer requireNonNegative(
            Integer value,
            String requiredMessage,
            String negativeMessage) {

        if (value == null) {
            throw new BadRequestException(requiredMessage);
        }

        if (value < 0) {
            throw new BadRequestException(negativeMessage);
        }

        return value;
    }
}

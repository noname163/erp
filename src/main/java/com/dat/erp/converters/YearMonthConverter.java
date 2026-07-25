package com.dat.erp.converters;

import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class YearMonthConverter
        implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth value) {
        return value == null ? null : value.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String value) {
        return value == null ? null : YearMonth.parse(value);
    }
}

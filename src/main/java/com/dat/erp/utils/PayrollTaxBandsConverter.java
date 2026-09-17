package com.dat.erp.utils;

import java.util.Arrays;
import java.util.List;
import com.dat.erp.data.PayrollTaxBand;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PayrollTaxBandsConverter implements AttributeConverter<List<PayrollTaxBand>, String> {
    public String convertToDatabaseColumn(List<PayrollTaxBand> value) { return value == null ? null : PayslipJson.write(value); }
    public List<PayrollTaxBand> convertToEntityAttribute(String value) {
        return value == null ? null : Arrays.asList(PayslipJson.read(value, PayrollTaxBand[].class));
    }
}

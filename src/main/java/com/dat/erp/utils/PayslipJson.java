package com.dat.erp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public final class PayslipJson {
    private static final ObjectMapper JSON = JsonMapper.builder().findAndAddModules().build();
    private PayslipJson() {}
    public static String write(Object value) {
        try { return JSON.writeValueAsString(value); }
        catch (Exception ex) { throw new IllegalStateException("Unable to save payroll calculation snapshot", ex); }
    }
    public static <T> T read(String value, Class<T> type) {
        try { return JSON.readValue(value, type); }
        catch (Exception ex) { throw new IllegalStateException("Unable to read payroll calculation snapshot", ex); }
    }
}

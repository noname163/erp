package com.dat.erp.contexts;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_COMPANY = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCompanyCode(String companyCode) {
        CURRENT_COMPANY.set(companyCode);
    }

    public static String requireCompanyCode() {
        String companyCode = CURRENT_COMPANY.get();

        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalStateException(
                    "Company context is missing");
        }

        return companyCode;
    }

    public static void clear() {
        CURRENT_COMPANY.remove();
    }
}
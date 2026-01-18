package com.dat.erp.constants;

/**
 * Centralized string constants for common API messages and errors.
 */
public final class Messages {
    private Messages() {
    }

    // Generic
    public static final String OK = "OK";
    public static final String CREATED = "Created";
    public static final String SUCCESS = "Success";

    // Generic validation/errors
    public static final String ERROR_BAD_REQUEST_NULL_ROLE_REQUEST = "RoleRequest cannot be null";

    public static final String ERROR_ACCOUNT_NOT_FOUND_WITH_CODE = "Account with code %s not found";
    public static final String ERROR_USER_PROFILE_NOT_FOUND_WITH_ACCOUNT = "User profile for account %s not found";
    public static final String ERROR_ACCOUNT_EMAIL_EXISTS = "Account email already exists";
    public static final String ERROR_ROLE_NOT_FOUND_WITH_CODE = "Role not found with code %s";
    public static final String ERROR_DEPARTMENT_NOT_FOUND_WITH_CODE = "Department not found with code %s";
    public static final String ERROR_CURRENT_USER_COMPANY_MISSING = "Current user company is missing";
    public static final String ERROR_CANNOT_CREATE_ADMIN_OR_MANAGER_EMPLOYEE = "Cannot create employee with ADMIN or SYSTEM_ADMIN role";
    // Authentication
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid credentials";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";

    // Departments/Companies
    public static final String ERROR_COMPANY_NOT_FOUND_WITH_CODE = "Unable to find company with code %s";

    // Conflict messages
    public static final String ERROR_COMPANY_NAME_EXISTS = "Company name already exists";
    public static final String ERROR_COMPANY_EMAIL_EXISTS = "Company email already exists";
    public static final String ERROR_COMPANY_TAX_NUMBER_EXISTS = "Company tax number already exists";
    public static final String ERROR_DEPARTMENT_NAME_EXISTS = "Department name already exists";
}

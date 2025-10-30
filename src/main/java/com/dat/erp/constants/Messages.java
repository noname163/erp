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
    public static final String ERROR_BAD_REQUEST_NULL_ROLE_HAS_API_REQUEST = "RoleHasApiRequest cannot be null";

    // Work schedule
    public static final String WORK_SCHEDULE_CREATED_WITH_EMPLOYEES =
            "WorkSchedule with Employees created successfully";
    public static final String ERROR_WORK_SCHEDULE_NOT_FOUND = "WorkSchedule not found";

    // Employee schedule
    public static final String EMPLOYEE_HAS_WORK_SCHEDULE_CREATED =
            "EmployeeHasWorkSchedule created successfully";
    public static final String ERROR_EMPLOYEE_NOT_FOUND = "EmployeeInformation not found";

    // Authentication
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid credentials";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";

    // Departments/Companies/Employees
    public static final String ERROR_COMPANY_NOT_FOUND_WITH_CODE = "Unable to find company with code %s";
    public static final String ERROR_EMPLOYEE_NOT_FOUND_WITH_CODE = "Not existed employee with code %s";
    public static final String ERROR_EMPLOYEE_NOT_FOUND_WITH_CODE_ALT = "Employee with code %s not found";

    // Role-API
    public static final String ROLE_APIS_ASSIGNED_SUCCESS = "APIs assigned to role successfully";

    // Conflict messages
    public static final String ERROR_ROLE_NAME_EXISTS = "Role name already exists";
    public static final String ERROR_COMPANY_NAME_EXISTS = "Company name already exists";
    public static final String ERROR_DEPARTMENT_NAME_EXISTS = "Department name already exists";
    public static final String ERROR_USER_EMAIL_EXISTS = "User email already exists";
    public static final String ERROR_EMPLOYEE_EMAIL_EXISTS = "Employee email already exists";
    public static final String ERROR_ROLE_API_MAPPING_EXISTS = "API already assigned to role";
    public static final String ERROR_EMPLOYEE_SCHEDULE_EXISTS = "Employee already assigned to this schedule";
    public static final String ERROR_WORK_SCHEDULE_EXISTS = "Work schedule already exists";
}

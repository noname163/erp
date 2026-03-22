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
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successful";
    public static final String ERROR_OLD_PASSWORD_INCORRECT = "Old password is incorrect";
    public static final String ERROR_PASSWORD_CONFIRM_MISMATCH = "confirmPassword does not match newPassword";

    // Departments/Companies
    public static final String ERROR_COMPANY_NOT_FOUND_WITH_CODE = "Unable to find company with code %s";

    // Conflict messages
    public static final String ERROR_COMPANY_NAME_EXISTS = "Company name already exists";
    public static final String ERROR_COMPANY_EMAIL_EXISTS = "Company email already exists";
    public static final String ERROR_COMPANY_TAX_NUMBER_EXISTS = "Company tax number already exists";
    public static final String ERROR_DEPARTMENT_NAME_EXISTS = "Department name already exists";

    // Salary templates
    public static final String ERROR_SALARY_TEMPLATE_NAME_EXISTS = "Salary template already exists";
    public static final String ERROR_SALARY_TEMPLATE_NOT_FOUND_WITH_CODE = "Salary template not found with code %s";
    public static final String ERROR_SALARY_TEMPLATE_EFFECTIVE_DATES_INVALID = "effectiveFrom/effectiveTo is invalid";
    public static final String ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_INVALID = "totalAmount is invalid";
    public static final String ERROR_SALARY_TEMPLATE_DETAILS_INVALID = "details is invalid";
    public static final String ERROR_SALARY_TEMPLATE_DETAIL_SALARY_CODE_INVALID = "salaryCode is invalid";
    public static final String ERROR_SALARY_TEMPLATE_DETAIL_UNIT_CODE_INVALID = "unitCode is invalid";
    public static final String ERROR_SALARY_TEMPLATE_DETAIL_AMOUNT_INVALID = "amount is invalid";
    public static final String ERROR_SALARY_TEMPLATE_DETAIL_QUANTITY_INVALID = "quantity is invalid";
    public static final String ERROR_SALARY_TEMPLATE_DETAIL_SEQUENCE_ORDER_INVALID = "sequenceOrder is invalid";
    public static final String ERROR_SALARY_TEMPLATE_TOTAL_AMOUNT_MISMATCH = "totalAmount does not match details";

    // Salaries
    public static final String ERROR_SALARY_REQUESTS_INVALID = "requests is invalid";
    public static final String ERROR_SALARY_NAME_INVALID = "name is invalid";
    public static final String ERROR_SALARY_CALCULATE_METHOD_INVALID = "calculateMethod is invalid";
    public static final String ERROR_SALARY_IS_DEDUCT_INVALID = "isDeduct is invalid";
    public static final String ERROR_SALARY_NAME_EXISTS = "Salary already exists";

    // Employee salary
    public static final String ERROR_EMPLOYEE_SALARY_EFFECTIVE_DATES_INVALID = "effectiveFrom/effectiveTo is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_TOTAL_AMOUNT_INVALID = "totalAmount is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_AMOUNT_RANGE_INVALID = "amount range is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_CURRENCY_INVALID = "currency is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_USER_PROFILE_CODE_INVALID = "userProfileCode is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_EMPLOYEE_NOT_FOUND = "Employee not found";
    public static final String ERROR_EMPLOYEE_SALARY_EMPLOYEE_INACTIVE = "Employee is inactive";
    public static final String ERROR_EMPLOYEE_SALARY_PERIOD_OVERLAPS = "Employee salary period overlaps";
    public static final String ERROR_EMPLOYEE_SALARY_EMPLOYEE_COMPANY_MISMATCH = "Employee does not belong to current company";
    public static final String ERROR_EMPLOYEE_SALARY_COMPANY_SECRET_KEY_MISSING = "Company secret key is missing";

    // Employee salary detail
    public static final String ERROR_EMPLOYEE_SALARY_DETAILS_INVALID = "requests is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_CODE_INVALID = "employeeSalaryCode is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_SALARY_CODE_INVALID = "salaryCode is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_INVALID = "dependenceCode is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_DEPENDENCE_CODE_MUST_EXIST_IN_REQUEST = "dependenceCode must exist in request list";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_UNIT_CODE_INVALID = "unitCode is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID = "amount is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_QUANTITY_INVALID = "quantity is invalid";
    public static final String ERROR_EMPLOYEE_SALARY_NOT_FOUND_WITH_CODE = "Employee salary not found with code %s";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_EMPLOYEE_SALARY_COMPANY_MISMATCH = "Employee salary does not belong to current company";
    public static final String ERROR_EMPLOYEE_SALARY_DETAIL_ALREADY_EXISTS = "Employee salary detail already exists";
    public static final String EMPLOYEE_SALARY_DETAIL_CREATE_SUCCESS = "Create salary detail for %s success";

    // Daily work
    public static final String ERROR_DAILY_WORK_REQUESTS_INVALID = "requests is invalid";
    public static final String ERROR_DAILY_WORK_USER_PROFILE_CODE_INVALID = "userProfileCode is invalid";
    public static final String ERROR_DAILY_WORK_WORKING_DATE_INVALID = "workingDate is invalid";
    public static final String ERROR_DAILY_WORK_START_END_TIME_INVALID = "startTime/endTime is invalid";
    public static final String ERROR_DAILY_WORK_OT_TIME_INVALID = "otTime is invalid";
    public static final String ERROR_DAILY_WORK_QUANTITY_INVALID = "quantity is invalid";
    public static final String ERROR_DAILY_WORK_UNIT_INVALID = "unit is invalid";
    public static final String ERROR_DAILY_WORK_WORK_TYPE_INVALID = "workType is invalid";
    public static final String ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND = "Employee not found";
    public static final String ERROR_DAILY_WORK_EMPLOYEE_INACTIVE = "Employee is inactive";
    public static final String ERROR_DAILY_WORK_EMPLOYEE_COMPANY_MISMATCH = "Employee does not belong to current company";
    public static final String ERROR_DAILY_WORK_ALREADY_EXISTS = "Daily work already exists";
    public static final String DAILY_WORK_CREATE_SUCCESS = "Create daily work for %s success";

    // Payroll
    public static final String ERROR_PAYROLL_PERIOD_INVALID = "payroll period is invalid";
    public static final String ERROR_PAYROLL_POLICY_NOT_FOUND = "Payroll policy not found";
    public static final String ERROR_PAYROLL_POLICY_OVERLAPS = "Payroll policy period overlaps";
    public static final String ERROR_PAYROLL_POLICY_NAME_INVALID = "Payroll policy name is invalid";
    public static final String ERROR_PAYROLL_RUN_NOT_FOUND = "Payroll run not found";
    public static final String ERROR_PAYROLL_RUN_STATUS_INVALID = "Payroll run status is invalid";
    public static final String ERROR_PAYROLL_RUN_FINALIZE_BLOCKED = "Payroll run cannot be finalized because blocking issues exist";
    public static final String ERROR_PAYROLL_RUN_REPLAY_SOURCE_INVALID = "Only finalized payroll runs can be replayed";
    public static final String ERROR_PAYROLL_NO_SALARY_RECORD = "No active salary record found";
    public static final String ERROR_PAYROLL_OVERLAPPING_SALARY_RECORD = "Overlapping salary record found";
    public static final String ERROR_PAYROLL_SALARY_GAP = "Gap between salary records found";
    public static final String ERROR_PAYROLL_MISSING_SCHEDULE = "No active work schedule found";
    public static final String ERROR_PAYROLL_MISSING_CALENDAR = "No active company calendar found";
    public static final String ERROR_PAYROLL_ZERO_DENOMINATOR = "Payroll denominator resolved to zero";
    public static final String ERROR_PAYROLL_INVALID_MULTIPLIER = "Invalid payroll multiplier";
    public static final String ERROR_PAYROLL_CONFLICTING_DAY_INPUT = "Conflicting payroll day inputs";
    public static final String ERROR_PAYROLL_ADJUSTMENT_NOT_FOUND = "Payroll adjustment not found";
    public static final String ERROR_PAYROLL_ADJUSTMENT_REASON_REQUIRED = "Payroll adjustment reason is required";
    public static final String ERROR_WORK_SCHEDULE_NOT_FOUND = "Work schedule not found";
    public static final String ERROR_WORK_SCHEDULE_NAME_INVALID = "Work schedule name is invalid";
    public static final String ERROR_WORK_SCHEDULE_DETAILS_INVALID = "Work schedule details are invalid";
    public static final String ERROR_COMPANY_CALENDAR_NOT_FOUND = "Company calendar not found";
    public static final String ERROR_COMPANY_CALENDAR_NAME_INVALID = "Company calendar name is invalid";
    public static final String ERROR_COMPANY_CALENDAR_DATES_INVALID = "Company calendar dates are invalid";
    public static final String ERROR_PAYROLL_SELF_VIEW_FORBIDDEN = "Cannot view another employee payslip";
    public static final String SUCCESS_PAYROLL_PREVIEW_QUEUED = "Payroll preview queued";
    public static final String SUCCESS_PAYROLL_FINALIZED = "Payroll finalized";
    public static final String SUCCESS_PAYROLL_REPLAY_QUEUED = "Payroll replay queued";
}

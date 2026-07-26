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

    // Company calendar
    public static final String ERROR_COMPANY_CALENDAR_NAME_INVALID = "name is invalid";
    public static final String ERROR_COMPANY_CALENDAR_EFFECTIVE_DATES_INVALID = "effectiveFrom/effectiveTo is invalid";
    public static final String ERROR_COMPANY_CALENDAR_DATES_INVALID = "dates is invalid";
    public static final String ERROR_COMPANY_CALENDAR_REGION_INVALID = "region is invalid";
    public static final String ERROR_COMPANY_CALENDAR_TIME_ZONE_INVALID = "timeZone is invalid";
    public static final String ERROR_COMPANY_CALENDAR_NOTE_INVALID = "note is invalid";
    public static final String ERROR_COMPANY_CALENDAR_CODE_INVALID = "code is invalid";
    public static final String ERROR_COMPANY_CALENDAR_NOT_FOUND = "Company calendar not found";
    public static final String ERROR_COMPANY_CALENDAR_YEAR_INVALID = "year is invalid";
    public static final String ERROR_COMPANY_CALENDAR_COMPANY_CODE_INVALID = "companyCode is invalid";
    public static final String ERROR_COMPANY_CALENDAR_MONTH_INVALID = "month is invalid";
    public static final String ERROR_COMPANY_CALENDAR_CAL_DATE_INVALID = "calDate is invalid";
    public static final String ERROR_COMPANY_CALENDAR_DAY_TYPE_INVALID = "dayType is invalid";
    public static final String ERROR_COMPANY_CALENDAR_DATE_NOTE_INVALID = "date note is invalid";
    public static final String ERROR_COMPANY_CALENDAR_DATE_DUPLICATE = "calDate must be unique";
    public static final String ERROR_COMPANY_CALENDAR_DATE_OUT_OF_RANGE = "calDate must be within effectiveFrom/effectiveTo";

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

    // Payroll policy
    public static final String ERROR_PAYROLL_POLICY_NAME_INVALID = "name is invalid";
    public static final String ERROR_PAYROLL_POLICY_STANDARD_QUANTITY_PER_DAY_INVALID = "standardQuantityPerDay is invalid";
    public static final String ERROR_PAYROLL_POLICY_UNIT_CODE_INVALID = "unitCode is invalid";
    public static final String ERROR_PAYROLL_POLICY_STANDARD_TIME_INVALID = "standardStartTime/standardEndTime is invalid";
    public static final String ERROR_PAYROLL_POLICY_ROUNDING_RULE_INVALID = "roundingRule is invalid";
    public static final String ERROR_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID = "effectiveFrom/effectiveTo is invalid";
    public static final String ERROR_PAYROLL_POLICY_NAME_EXISTS = "Payroll policy already exists";

    // Employee payroll policy
    public static final String ERROR_EMPLOYEE_PAYROLL_POLICY_NOT_FOUND = "Employee payroll policy not found";
    public static final String ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODE_INVALID = "userProfileCode is invalid";
    public static final String ERROR_EMPLOYEE_PAYROLL_POLICY_USER_PROFILE_CODES_INVALID = "userProfileCodes is invalid";
    public static final String ERROR_EMPLOYEE_PAYROLL_POLICY_POLICY_CODE_INVALID = "payrollPolicyCode is invalid";
    public static final String ERROR_EMPLOYEE_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID = "effectiveFrom/effectiveTo is invalid";
    public static final String ERROR_EMPLOYEE_PAYROLL_POLICY_OVERLAPS = "Employee already has an active payroll policy in this period";

    // Monthly salary calculation
    public static final String ERROR_PAYROLL_MONTH_INVALID = "month is invalid";
    public static final String ERROR_PAYROLL_EMPLOYEE_CODE_INVALID = "employeeCode is invalid";
    public static final String ERROR_PAYROLL_EMPLOYEE_SALARY_NOT_FOUND = "No active employee salary found for employee %s in %s";
    public static final String ERROR_PAYROLL_POLICY_NOT_FOUND = "No active payroll policy found for employee %s in %s";
    public static final String ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID = "expectedWorkingHourPerMonth must be greater than zero";
    public static final String ERROR_PAYROLL_DEPENDENCE_CODE_MISSING = "Missing dependency salary detail for code %s";
    public static final String ERROR_PAYROLL_DAY_TYPE_HOURS_MISSING = "Missing working-hour mapping for dayType %s";
    public static final String ERROR_PAYROLL_CIRCULAR_DEPENDENCY = "Circular dependency detected in salary details: %s";

    // Payroll run
    public static final String ERROR_PAYROLL_RUN_ALREADY_EXISTS = "Payroll run already exists for requested period";
    public static final String ERROR_PAYROLL_RUN_CODE_INVALID = "payrollRunCode is invalid";
    public static final String ERROR_PAYROLL_RUN_MONTH_TOO_OLD = "runDate must not be earlier than current month minus 3 months";
    public static final String ERROR_PAYROLL_RUN_RUN_AT_RANGE_INVALID = "runAt range is invalid";
    public static final String ERROR_PAYROLL_RUN_CLOSE_AT_RANGE_INVALID = "closeAt range is invalid";
    public static final String ERROR_PAYROLL_RUN_NOT_FOUND = "Payroll run not found with code %s";
    public static final String ERROR_PAYROLL_RERUN_REASON_REQUIRED = "reason is required";
    public static final String ERROR_PAYROLL_RERUN_MODE_INVALID = "mode is invalid";
    public static final String ERROR_PAYROLL_RERUN_EMPLOYEES_REQUIRED = "employeeCodes is required for selected employees mode";
    public static final String ERROR_PAYROLL_RERUN_CLOSED_NOT_ALLOWED = "Closed payroll run cannot be re-run";
    public static final String ERROR_PAYROLL_RERUN_CONCURRENT = "Payroll run is already processing or rerunning";
    public static final String ERROR_PAYROLL_RERUN_FAILED_ONLY_UNSUPPORTED = "FAILED_ONLY mode is not supported by current payroll result status model";

    // Payroll result
    public static final String ERROR_PAYROLL_RESULT_CODE_INVALID = "payrollResultCode is invalid";
    public static final String ERROR_PAYROLL_RESULT_NOT_FOUND = "Payroll result not found with code %s";
    public static final String ERROR_PAYROLL_RESULT_NULL = "Payroll result can not bale to be null";
}

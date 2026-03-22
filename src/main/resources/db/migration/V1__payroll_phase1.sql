create table if not exists employment_agreement (
    id bigserial primary key,
    code varchar(255) not null unique,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(255),
    updated_by varchar(255),
    company_code varchar(255),
    is_deleted boolean not null default false,
    user_profile_code varchar(255) not null,
    employment_type varchar(100),
    employment_status varchar(100),
    effective_from date,
    effective_to date,
    resignation_date date,
    termination_date date,
    suspension_from date,
    suspension_to date,
    eligible_for_payroll boolean,
    eligible_for_final_settlement boolean,
    eligible_for_retro_only boolean,
    attendance_tracking_mode varchar(255),
    notes varchar(2000)
);

create table if not exists work_schedule (
    id bigserial primary key,
    code varchar(255) not null unique,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(255),
    updated_by varchar(255),
    company_code varchar(255),
    is_deleted boolean not null default false,
    name varchar(255),
    description varchar(1000),
    schedule_type varchar(255),
    standard_hours_per_day integer,
    standard_minutes_per_day integer,
    flexible_working_hours boolean,
    cross_midnight_allowed boolean,
    effective_from date,
    effective_to date
);

create table if not exists work_schedule_detail (
    id bigserial primary key,
    code varchar(255) not null unique,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(255),
    updated_by varchar(255),
    company_code varchar(255),
    is_deleted boolean not null default false,
    work_schedule_code varchar(255) not null,
    day_of_week integer,
    is_working_day boolean,
    start_time time,
    end_time time,
    break_minutes integer,
    paid_break boolean,
    full_day_threshold_minutes integer
);

create table if not exists employee_schedule_assignment (
    id bigserial primary key,
    code varchar(255) not null unique,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(255),
    updated_by varchar(255),
    company_code varchar(255),
    is_deleted boolean not null default false,
    user_profile_code varchar(255),
    work_schedule_code varchar(255) not null,
    department_code varchar(255),
    location_code varchar(255),
    effective_from date,
    effective_to date,
    priority integer
);

create table if not exists payroll_adjustment (
    id bigserial primary key,
    code varchar(255) not null unique,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(255),
    updated_by varchar(255),
    company_code varchar(255),
    is_deleted boolean not null default false,
    user_profile_code varchar(255) not null,
    salary_code varchar(255),
    adjustment_type varchar(100),
    approval_status varchar(100),
    amount varchar(255),
    quantity numeric(19, 2),
    unit_code varchar(255),
    reason varchar(2000),
    source_month varchar(20),
    effective_payroll_month varchar(20),
    is_retro boolean,
    reference_code varchar(255),
    effective_date date
);

create table if not exists payroll_employee_summary (
    id bigserial primary key,
    code varchar(255) not null unique,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(255),
    updated_by varchar(255),
    company_code varchar(255),
    is_deleted boolean not null default false,
    payroll_run_code varchar(255) not null,
    user_profile_code varchar(255) not null,
    gross_amount varchar(255),
    deduction_amount varchar(255),
    net_amount varchar(255),
    currency varchar(50),
    status varchar(100),
    has_blocking_issue boolean,
    issue_message varchar(2000),
    policy_snapshot_version varchar(255),
    is_frozen boolean
);

alter table if exists payroll_policy add column if not exists name varchar(255);
alter table if exists payroll_policy add column if not exists standard_hours_per_day integer;
alter table if exists payroll_policy add column if not exists standard_minutes_per_day integer;
alter table if exists payroll_policy add column if not exists rounding_mode varchar(255);
alter table if exists payroll_policy add column if not exists round_at varchar(255);
alter table if exists payroll_policy add column if not exists zero_denominator_action varchar(255);
alter table if exists payroll_policy add column if not exists holiday_weekend_overlap_rule varchar(255);
alter table if exists payroll_policy add column if not exists approval_mode varchar(255);
alter table if exists payroll_policy add column if not exists freeze_snapshot_required boolean;
alter table if exists payroll_policy add column if not exists night_premium_start time;
alter table if exists payroll_policy add column if not exists night_premium_end time;
alter table if exists payroll_policy add column if not exists ot_requires_approval boolean;
alter table if exists payroll_policy add column if not exists ot_minimum_minutes integer;
alter table if exists payroll_policy add column if not exists ot_rounding_minutes integer;

alter table if exists pay_rate_rule add column if not exists rate_type varchar(100);
alter table if exists pay_rate_rule add column if not exists priority integer;
alter table if exists pay_rate_rule add column if not exists rate_name varchar(255);
alter table if exists pay_rate_rule add column if not exists start_time time;
alter table if exists pay_rate_rule add column if not exists end_time time;
alter table if exists pay_rate_rule add column if not exists minimum_minutes integer;
alter table if exists pay_rate_rule add column if not exists rounding_minutes integer;
alter table if exists pay_rate_rule add column if not exists requires_approval boolean;
alter table if exists pay_rate_rule add column if not exists department_code varchar(255);
alter table if exists pay_rate_rule add column if not exists location_code varchar(255);
alter table if exists pay_rate_rule add column if not exists employment_type varchar(100);
alter table if exists pay_rate_rule add column if not exists salary_code varchar(255);
alter table if exists pay_rate_rule add column if not exists is_stackable boolean;

alter table if exists salary add column if not exists component_type varchar(100);
alter table if exists salary add column if not exists default_proration_basis varchar(100);
alter table if exists salary add column if not exists taxable boolean;
alter table if exists salary add column if not exists manual_entry_allowed boolean;
alter table if exists salary add column if not exists multiplier_eligible boolean;

alter table if exists employee_salary_detail add column if not exists quantity numeric(19, 2);
alter table if exists employee_salary_detail add column if not exists sequence_order integer;
alter table if exists employee_salary_detail add column if not exists proration_basis_override varchar(100);
alter table if exists employee_salary_detail add column if not exists is_prorated boolean;
alter table if exists employee_salary_detail add column if not exists is_multiplier_eligible boolean;
alter table if exists employee_salary_detail add column if not exists component_type varchar(100);
alter table if exists employee_salary_detail add column if not exists daily_work_work_type varchar(100);

alter table if exists daily_work add column if not exists approval_status varchar(100);
alter table if exists daily_work add column if not exists break_minutes integer;
alter table if exists daily_work add column if not exists paid_break boolean;
alter table if exists daily_work add column if not exists attendance_corrected boolean;
alter table if exists daily_work add column if not exists approved_at timestamp;

alter table if exists employee_pto add column if not exists leave_type varchar(255);
alter table if exists employee_pto add column if not exists approval_status varchar(100);
alter table if exists employee_pto add column if not exists paid_mode varchar(100);
alter table if exists employee_pto add column if not exists duration_unit varchar(100);
alter table if exists employee_pto add column if not exists hours numeric(19, 2);

alter table if exists payroll_run add column if not exists period_start date;
alter table if exists payroll_run add column if not exists period_end date;
alter table if exists payroll_run add column if not exists pay_date date;
alter table if exists payroll_run add column if not exists is_preview boolean;
alter table if exists payroll_run add column if not exists snapshot_version varchar(255);
alter table if exists payroll_run add column if not exists replayed_from_run_code varchar(255);
alter table if exists payroll_run add column if not exists approval_status varchar(100);
alter table if exists payroll_run add column if not exists warning_count integer;
alter table if exists payroll_run add column if not exists error_count integer;

alter table if exists payroll_result add column if not exists employee_summary_code varchar(255);
alter table if exists payroll_result add column if not exists quantity_value numeric(19, 2);
alter table if exists payroll_result add column if not exists line_type varchar(100);
alter table if exists payroll_result add column if not exists segment_from date;
alter table if exists payroll_result add column if not exists segment_to date;
alter table if exists payroll_result add column if not exists currency varchar(50);
alter table if exists payroll_result add column if not exists rate varchar(255);
alter table if exists payroll_result add column if not exists multiplier numeric(10, 4);
alter table if exists payroll_result add column if not exists sequence_order integer;
alter table if exists payroll_result add column if not exists source_ref_code varchar(255);
alter table if exists payroll_result add column if not exists policy_snapshot_version varchar(255);
alter table if exists payroll_result add column if not exists is_manual boolean;
alter table if exists payroll_result add column if not exists is_frozen boolean;

alter table if exists payroll_result_detail add column if not exists basis_hours numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists basis_minutes numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists payable_hours numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists payable_minutes numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists expected_hours numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists expected_minutes numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists expected_quantity numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists payable_quantity numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists rate_per_hour numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists rate_per_minute numeric(19, 2);
alter table if exists payroll_result_detail add column if not exists rounding_note varchar(2000);
alter table if exists payroll_result_detail add column if not exists policy_rule_code varchar(255);
alter table if exists payroll_result_detail add column if not exists source_date date;

create index if not exists idx_employment_agreement_user_profile on employment_agreement (user_profile_code, effective_from, effective_to);
create index if not exists idx_employee_schedule_assignment_user_profile on employee_schedule_assignment (user_profile_code, effective_from, effective_to);
create index if not exists idx_payroll_adjustment_user_period on payroll_adjustment (user_profile_code, effective_payroll_month);
create index if not exists idx_payroll_employee_summary_run on payroll_employee_summary (payroll_run_code, user_profile_code);

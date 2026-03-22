# Payroll Calculation Solution Proposal

This document proposes a month-end payroll design that fits the current ERP codebase and the scenarios listed by the user.

It is intentionally shaped around the entities already present in the project:

- `UserProfile`
- `EmployeeSalary`
- `EmployeeSalaryDetail`
- `Salary`
- `PayrollPolicy`
- `PayRateRule`
- `DailyWork`
- `EmployeePto`
- `CompanyCalendar`
- `CalendarDate`
- `PayrollRun`
- `PayrollResult`
- `PayrollResultDetail`

The goal is to support a real payroll engine without breaking the existing employee, salary-template, employee-salary, and daily-work logic.

## 1. Core Design Principles

1. Keep existing working flows intact.
   - Employee creation, salary assignment, salary template creation, and daily work logging should continue to work as-is.
   - New payroll behavior should be additive.

2. Separate source data from payroll output.
   - Source data: employee lifecycle, salary agreements, attendance, leave, schedule, calendar, manual adjustments.
   - Output data: payroll run, payroll lines, calculation details, frozen snapshot.

3. Calculate by effective-dated segments, not by one whole-month assumption.
   - Join date, resign date, salary change, policy change, schedule change, holiday rule change, and retro input can all split the month.

4. Make every payroll line explainable.
   - Each line should preserve source, quantity, unit, multiplier, formula note, and policy version.

5. Freeze finalized payroll.
   - Preview can be recalculated.
   - Finalized payroll should not change unless explicitly reopened or recalculated.

## 2. What the Current Model Already Supports

The current project already has a usable backbone:

- `EmployeeSalary` and `EmployeeSalaryDetail`
  - effective-dated salary assignment
  - salary components
- `DailyWork`
  - worked date
  - start/end time
  - overtime minutes
  - work type
  - hours worked
- `EmployeePto`
  - leave period
  - leave status
- `CompanyCalendar` and `CalendarDate`
  - company-level working/holiday calendar
- `PayrollPolicy` and `PayRateRule`
  - starting point for proration and day-type multiplier rules
- `PayrollRun`, `PayrollResult`, `PayrollResultDetail`
  - existing shell for payroll output and explainability

That means the engine does not need to start from zero. It needs better rule resolution and a few missing data structures.

## 3. Main Gaps Against Real Payroll

The current model does not yet represent these areas strongly enough:

- employment lifecycle
  - suspended
  - resigned
  - terminated
  - final settlement eligibility
  - inactive with retro-only eligibility
- employment type
  - full-time
  - part-time
  - probation
  - intern
  - contractor
  - custom agreement
- work schedule
  - standard hours/day
  - rotating shift
  - custom employee schedule
  - schedule effective date changes
- approval state
  - approved OT only
  - approved leave only
  - pending correction excluded
- manual inputs
  - bonus
  - deduction
  - retro adjustment
  - final settlement line
- policy snapshot freezing
  - finalized payroll should preserve exactly which rule version was used

## 4. Recommended Domain Strategy

## 4.1 Reuse existing entities as the main payroll backbone

Use the following entities as authoritative output and core inputs:

- `UserProfile`
  - employee identity
- `EmployeeSalary` and `EmployeeSalaryDetail`
  - salary agreement and components
- `DailyWork`
  - actual attendance/work quantity
- `EmployeePto`
  - paid/unpaid leave inputs
- `CompanyCalendar` and `CalendarDate`
  - holiday/weekend/workday meaning
- `PayrollPolicy` and `PayRateRule`
  - proration, multiplier, and premium rules
- `PayrollRun`
  - payroll execution header
- `PayrollResult`
  - payroll line item
- `PayrollResultDetail`
  - formula explanation and basis

## 4.2 Add new entities instead of overloading current ones too hard

Recommended new entities:

### `EmploymentAgreement`

Purpose:

- effective-dated employment status and employment type

Suggested fields:

- `userProfileCode`
- `employmentType`
- `employmentStatus`
- `effectiveFrom`
- `effectiveTo`
- `terminationDate`
- `resignationDate`
- `suspensionFrom`
- `suspensionTo`
- `eligibleForPayroll`
- `eligibleForFinalSettlement`
- `eligibleForRetroOnly`
- `attendanceTrackingMode`
- `notes`

Why:

- `UserProfile.isActive` alone is too weak for join/resign/suspend/terminate/final-settlement scenarios.

### `WorkSchedule`

Purpose:

- defines standard expected work

Suggested fields:

- `name`
- `scheduleType`
- `standardHoursPerDay`
- `standardMinutesPerDay`
- `breakPolicy`
- `flexibleTimeAllowed`
- `crossMidnightAllowed`
- `effectiveFrom`
- `effectiveTo`

### `WorkScheduleDetail`

Purpose:

- per weekday or per shift rule

Suggested fields:

- `scheduleCode`
- `dayOfWeek`
- `isWorkingDay`
- `startTime`
- `endTime`
- `breakMinutes`
- `paidBreak`
- `minHoursForFullDay`
- `defaultUnit`

### `EmployeeScheduleAssignment`

Purpose:

- effective-dated employee or department schedule assignment

Suggested fields:

- `userProfileCode`
- `departmentCode`
- `locationCode`
- `scheduleCode`
- `effectiveFrom`
- `effectiveTo`
- `priority`

### `PayrollAdjustment`

Purpose:

- manual and retro inputs not sourced from attendance directly

Suggested fields:

- `userProfileCode`
- `salaryCode`
- `adjustmentType`
- `amount`
- `quantity`
- `unitCode`
- `reason`
- `sourceMonth`
- `effectivePayrollMonth`
- `approvalStatus`
- `isRetro`
- `referenceCode`

### `PayrollEmployeeSummary`

Purpose:

- optional employee-level header for payslip totals

Suggested fields:

- `payrollRunCode`
- `userProfileCode`
- `grossAmount`
- `deductionAmount`
- `netAmount`
- `currency`
- `status`
- `policySnapshotVersion`

Why:

- current `PayrollResult` is better treated as line-level output, not summary.

## 4.3 Additive updates to existing entities

These changes are safe if added as nullable fields and not made mandatory in current create flows.

### `PayrollPolicy`

Current fields are too small. Add:

- `name`
- `standardHoursPerDay`
- `standardMinutesPerDay`
- `roundingMode`
- `roundAt`
- `zeroDenominatorAction`
- `holidayWeekendOverlapRule`
- `approvalMode`
- `freezeSnapshotRequired`
- `nightPremiumStart`
- `nightPremiumEnd`
- `otRequiresApproval`
- `otMinimumMinutes`
- `otRoundingMinutes`

### `PayRateRule`

Add:

- `priority`
- `rateName`
- `rateType`
- `startTime`
- `endTime`
- `minimumMinutes`
- `roundingMinutes`
- `requiresApproval`
- `departmentCode`
- `locationCode`
- `employmentType`
- `salaryCode`
- `isStackable`

This allows:

- OT on holiday
- OT after 7PM
- night shift premium
- department-only premium
- premium windows

### `Salary`

Add:

- `componentType`
- `defaultProrationMode`
- `defaultUnitCode`
- `taxable`
- `affectsGross`
- `affectsNet`
- `isAttendanceBased`
- `isPremiumEligible`
- `allowNegative`
- `displayOrder`

### `EmployeeSalaryDetail`

Add:

- `unitCode`
- `quantity`
- `prorationModeOverride`
- `isProrated`
- `isMultiplierEligible`
- `formulaExpression`
- `sequenceOrder`
- `componentCategory`

Reason:

- not all components should be prorated
- not all components should be multiplied on OT/holiday
- some components are formula-based or dependent

### `PayrollRun`

Add:

- `periodStart`
- `periodEnd`
- `payDate`
- `isPreview`
- `snapshotVersion`
- `recalculatedFromRunCode`
- `freezeReason`
- `approvalStatus`

### `PayrollResult`

Keep it as a line item and add:

- `employeeSummaryCode`
- `lineType`
- `lineCategory`
- `segmentFrom`
- `segmentTo`
- `currency`
- `rate`
- `multiplier`
- `sequenceOrder`
- `sourceRefCode`
- `policySnapshotVersion`
- `isManual`
- `isFrozen`

### `PayrollResultDetail`

Keep it for explainability and add:

- `basisHours`
- `basisMinutes`
- `payableHours`
- `payableMinutes`
- `expectedHours`
- `expectedMinutes`
- `ratePerHour`
- `ratePerMinute`
- `roundingNote`
- `policyRuleCode`
- `sourceDate`

## 5. Recommended Payroll Engine Flow

The engine should run in this order.

## 5.1 Build payroll scope

Input:

- `companyCode`
- `periodStart`
- `periodEnd`
- run type: preview or finalize
- target employees or all payroll-eligible employees

Selection rules:

- include employees active in any portion of the month
- include employees who resigned/terminated during the month
- exclude employees fully resigned before the month unless they have:
  - final settlement
  - retro adjustment
- include inactive employees only when a valid retro/adjustment exists
- include suspended employees, but payable units depend on policy and leave/suspension rules

## 5.2 Resolve authoritative source data

For each employee, resolve:

- employment agreement slice
- active salary record(s)
- active salary details
- active payroll policy
- schedule assignment
- calendar assignment
- approved leave
- approved attendance corrections
- approved OT inputs
- manual adjustments and retro

If any mandatory source is missing, record payroll issue and stop that employee:

- no salary record
- overlapping salary record
- no schedule
- no calendar
- overlapping policy
- invalid multiplier
- denominator zero

## 5.3 Slice the month into effective-dated segments

This is the most important design choice.

Create breakpoints from:

- payroll period start and end
- join date
- resignation date
- termination date
- suspension start/end
- salary effective from/to
- policy effective from/to
- schedule effective from/to
- calendar effective from/to

Then convert them into non-overlapping segments:

```text
2026-03-01 to 2026-03-10
2026-03-11 to 2026-03-20
2026-03-21 to 2026-03-31
```

Each segment uses exactly one resolved version of:

- employment status/type
- salary agreement
- policy
- schedule
- calendar

This is how you correctly support:

- join mid-month
- resign mid-month
- salary change mid-month
- policy change mid-month
- schedule change mid-month

## 5.4 Compute expected units for each segment

Expected units depend on proration basis and schedule:

- calendar days
- working days
- hours
- minutes
- attendance quantity

Recommended outputs:

- `expectedCalendarDays`
- `expectedWorkingDays`
- `expectedHours`
- `expectedMinutes`
- `expectedAttendanceQty`

Rules:

- use `CompanyCalendar` plus schedule to decide whether a date is payable workday, holiday, off day, substitute day, shutdown day
- exclude unpaid breaks from expected payable minutes if policy says so
- support cross-midnight schedules

## 5.5 Compute actual payable units for each segment

Use:

- `DailyWork`
- approved `EmployeePto`
- manual attendance corrections

Outputs:

- `workedDays`
- `workedHours`
- `workedMinutes`
- `paidLeaveDays`
- `paidLeaveHours`
- `unpaidLeaveDays`
- `absenceDays`
- `otHours`
- `otMinutes`
- `nightPremiumMinutes`
- `holidayWorkedDays`
- `weekendWorkedDays`

Conflict detection:

- present and leave on same date
- duplicate attendance
- overlapping work records
- OT on absent day
- leave beyond period bounds

## 5.6 Generate payroll lines by category

The cleanest approach is to generate lines in this order.

### A. Fixed salary and allowance lines

Examples:

- base salary
- meal allowance
- transport allowance
- responsibility allowance
- fixed bonus

Formula:

```text
lineAmount = componentAmount * payableUnits / denominatorUnits
```

Where:

- `componentAmount` comes from `EmployeeSalaryDetail.amount`
- `payableUnits` depends on proration rule
- `denominatorUnits` depends on policy basis

Examples:

- fixed monthly salary, no proration
  - full amount
- base salary prorated by working days
  - monthly amount x payable working days / expected working days
- allowance not prorated
  - full amount even if month is partial, if policy says so

### B. Holiday/weekend/day-off premium lines

Examples:

- holiday pay x2
- holiday pay x3
- weekend premium
- company off-day premium

Formula:

```text
premiumAmount = baseRate * premiumQuantity * multiplier
```

Where:

- multiplier comes from `PayRateRule`
- base rate comes from base salary component or configured earning base

### C. Overtime lines

Examples:

- OT on weekday
- OT on weekend
- OT on holiday
- OT on night shift

Rules:

- only after standard hours reached
- only if approved
- round to nearest 30 or 60 minutes
- ignore below minimum threshold if configured

### D. Night premium lines

Examples:

- after 19:00 until 06:00
- only worked minutes inside premium window

Rules:

- support cross-midnight
- support overlap with weekend/holiday rules
- allow either:
  - premium on regular hours
  - premium on OT only
  - premium on both

### E. Leave lines

Examples:

- paid leave line
- unpaid leave deduction line
- sick leave paid/unpaid
- partial PTO coverage and remainder unpaid

### F. Bonus and incentive lines

Examples:

- performance bonus
- attendance bonus
- commission
- one-time support payment

Rules:

- some are prorated
- some are taxable
- some require approval
- some are one-month-only

### G. Deduction lines

Examples:

- unpaid leave deduction
- absence deduction
- insurance
- tax
- loan repayment
- capped penalty

Rules:

- fixed amount
- percentage
- capped
- threshold-based
- optionally prevent net negative

### H. Retro and adjustment lines

Examples:

- missed OT from prior month
- backdated salary increase
- reversed wrong deduction

Rules:

- never mutate old finalized run silently
- current payroll may carry an extra retro line
- require reason and source month

## 5.7 Round and total

Recommended order:

1. calculate raw quantity
2. round quantity by policy
3. calculate raw money
4. round line money by policy
5. sum gross
6. sum deductions
7. calculate net
8. final round at payslip total if required

Support:

- half-up
- down
- up
- banker rounding
- line-level rounding
- final-only rounding
- integer currency or 2-decimal currency

## 5.8 Persist frozen result

Use:

- `PayrollRun` as the run header
- `PayrollEmployeeSummary` as optional employee summary
- `PayrollResult` as payslip line
- `PayrollResultDetail` as formula explanation

Finalized payroll should store:

- policy snapshot version
- rule identifiers used
- segment dates used
- source references used
- formula notes

## 6. Decision Rules for the Employment Cases

Use this as the employee eligibility rule table.

| Case | Include in run? | Amount basis |
|---|---|---|
| Active for full month | Yes | Full or prorated by policy |
| Joined before payroll month | Yes | Full or prorated by policy |
| Joined during payroll month | Yes | Clip from join date |
| Resigned during payroll month | Yes | Clip through last payable date |
| Resigned before payroll month | No, unless settlement/retro | Settlement or retro only |
| Suspended | Yes | Depends on suspension pay rule |
| Terminated with final settlement | Yes | Settlement lines plus regular earned amount |
| Inactive with retro adjustment | Yes | Retro lines only |

Recommended model rule:

- eligibility is not just `isActive`
- eligibility is effective-dated and reason-based

## 7. Handling Salary Basis Cases

Use `EmployeeSalary` as the salary header and `EmployeeSalaryDetail` as the line components.

Recommended rule order:

1. find all `EmployeeSalary` records overlapping the payroll month
2. validate no overlaps
3. if gap exists:
   - either error
   - or calculate only covered segment, based on policy
4. split segments by salary effective date
5. for each segment, resolve active detail lines

Component behavior:

- base salary
  - usually prorated
- allowance
  - prorated or non-prorated per component rule
- responsibility allowance
  - may depend on role/assignment status
- meal/transport
  - often attendance-based or working-day-based
- fixed bonus
  - one-time or monthly fixed
- deduction component
  - subtract from gross or net based on type

## 8. Proration Strategy

Do not use one global proration rule for everything.

Use two levels:

### Policy-level default proration

From `PayrollPolicy`:

- calendar days
- working days
- hours
- minutes
- attendance quantity

### Component-level override

From `EmployeeSalaryDetail` or `Salary`:

- no proration
- follow policy default
- custom proration mode

This solves:

- base salary prorated by working days
- meal allowance by attendance quantity
- transport allowance by actual days present
- fixed bonus not prorated

Denominator guardrails:

- if denominator is zero, raise payroll issue
- do not silently divide by zero

## 9. Schedule and Calendar Strategy

Recommended precedence:

1. employee schedule assignment
2. department schedule assignment
3. company default schedule

Recommended calendar precedence:

1. employee/location override calendar
2. department/location calendar
3. company calendar

Day classification should output one normalized type for each date:

- workday
- weekend off
- public holiday
- company holiday
- substitute holiday
- make-up working day
- shutdown day
- emergency closure day
- special premium day

Conflict rule:

- one date must resolve to one final normalized classification
- conflicting raw setup should become payroll issue, not silent guesswork

## 10. Attendance, OT, and Premium Strategy

### Attendance

Use `DailyWork` as the source of actual work.

Needed upgrades:

- allow approval/correction status
- detect duplicate and overlapping entries
- support exempt-from-attendance employees

### OT

OT should be calculated from:

- actual worked minutes
- scheduled standard minutes
- approved OT rule

OT rule inputs:

- day type
- minimum threshold
- rounding unit
- approval requirement
- break exclusion
- night overlap

### Night premium

Compute by intersecting actual worked intervals with premium windows:

```text
worked interval: 17:00-22:00
premium window: 19:00-06:00
premium minutes = 19:00-22:00
```

If shift crosses midnight:

- split internally into date-time windows
- apply holiday/weekend/day-type after interval resolution, not before

## 11. Leave and PTO Strategy

`EmployeePto` needs stronger semantics.

Recommended additions:

- `leaveType`
- `approvalStatus`
- `paidMode`
- `durationUnit`
- `hours`
- `isBalanceTracked`
- `sourceBalanceTransactionCode`

Rules:

- only approved leave affects finalized payroll by default
- paid leave increases payable units
- unpaid leave reduces payable units
- if PTO balance is insufficient:
  - partially paid
  - remainder unpaid
  - behavior depends on company rule

## 12. Retro and Adjustment Strategy

Retro must not rewrite old finalized results.

Recommended flow:

1. previous payroll remains unchanged
2. correction is entered as `PayrollAdjustment`
3. current payroll creates one or more retro lines
4. each retro line keeps:
   - source month
   - reason
   - original reference

This supports:

- missed OT
- corrected attendance
- backdated salary increase
- reversed wrong deduction

## 13. Approval and Workflow Rules

Recommended defaults for finalize mode:

- approved leave only
- approved OT only
- approved bonus/deduction only
- approved attendance correction only

Preview mode may allow optional inclusion of pending data, but that should be explicit and clearly labeled.

`PayrollRun.status` should support at least:

- `DRAFT`
- `PREVIEW`
- `FINALIZED`
- `REOPENED`
- `CANCELLED`

## 14. Error Handling Strategy

Payroll should produce issues, not silent wrong numbers.

Recommended issue types:

- no active salary record
- overlapping salary record
- salary gap
- no schedule
- no calendar
- duplicate attendance
- overlapping attendance
- leave and attendance conflict
- invalid multiplier
- denominator zero
- negative net not allowed
- null currency
- same date both holiday and workday

Recommendation:

- block finalize on critical issues
- allow preview with warnings

## 15. Output Design

The minimum useful payslip output should include:

- base salary line
- allowance lines
- holiday pay line
- weekend pay line
- OT line
- night premium line
- paid leave line
- unpaid leave deduction line
- bonus lines
- deduction lines
- retro lines
- gross total
- total deduction
- net total

Each line should preserve:

- source of calculation
- formula note
- quantity
- unit
- rate
- multiplier
- segment dates
- salary component code
- policy snapshot version

## 16. Suggested Minimal MVP

Do not try to deliver every scenario in one version.

## Phase 1

Deliver first:

- monthly payroll run header
- employee selection by active employment in month
- salary resolution with effective dates
- proration by calendar days or working days
- holiday/weekend detection from calendar
- paid leave and unpaid leave handling
- OT on weekday/weekend/holiday
- night premium window
- manual bonus and deduction
- retro adjustment line
- preview and finalize
- frozen snapshot fields

This phase covers most of the minimum test checklist.

## Phase 2

Add:

- schedule assignments by employee/department
- hours/minutes proration
- part-time and custom schedule
- approval workflow for OT/leave/adjustments
- final settlement handling
- inactive-with-retro-only handling

## Phase 3

Add:

- tax and insurance engine
- multi-currency
- exchange rates
- richer PTO balance/accrual/carry-over
- advanced premiums and overlapping rate windows

## 17. Proposed Calculation Pseudocode

```text
runPayroll(company, periodStart, periodEnd, mode):
  create payrollRun

  employees = loadEligibleEmployees(company, periodStart, periodEnd, mode)

  for each employee in employees:
    context = loadEmployeePayrollContext(employee, periodStart, periodEnd)

    issues = validateContext(context)
    if issues contain blockingIssue:
      recordIssues(payrollRun, employee, issues)
      continue

    segments = buildEffectiveSegments(context, periodStart, periodEnd)
    lines = []

    for each segment in segments:
      expectedUnits = computeExpectedUnits(segment)
      payableUnits = computePayableUnits(segment)

      lines += buildFixedComponentLines(segment, expectedUnits, payableUnits)
      lines += buildHolidayWeekendLines(segment, payableUnits)
      lines += buildOvertimeLines(segment)
      lines += buildNightPremiumLines(segment)
      lines += buildLeaveLines(segment)

    lines += loadManualBonusLines(employee, period)
    lines += loadManualDeductionLines(employee, period)
    lines += loadRetroLines(employee, period)
    lines = applyRounding(lines)

    summary = summarize(lines)
    persistSummaryAndLines(payrollRun, employee, summary, lines, context.snapshotVersion)

  if mode == FINALIZE and noBlockingIssues:
    finalize payrollRun
```

## 18. Minimum Entity/File Change Recommendation

If you want the safest path that does not disturb current features:

1. Do not replace current employee salary or daily work logic.
2. Add new payroll-focused entities and nullable fields only.
3. Keep current controllers untouched.
4. Build the payroll engine in new services:
   - `PayrollRunService`
   - `PayrollCalculationService`
   - `PayrollPolicyResolver`
   - `PayrollScheduleResolver`
   - `PayrollAttendanceResolver`
   - `PayrollAdjustmentService`
5. Add payroll-specific repositories without altering existing repository behavior.

This keeps today’s salary assignment and attendance features stable while enabling a much richer payroll engine.

## 19. Recommended Test Matrix for the First Real Release

At minimum, automate these:

- active employee, full month
- join mid-month
- resign mid-month
- resigned before month with no retro
- suspended employee
- final settlement employee
- retro-only inactive employee
- salary change in middle of month
- one paid leave day
- one unpaid leave day
- one public holiday off
- one holiday worked at x3
- one weekend worked at x2
- OT on weekday
- night premium after 19:00
- non-prorated allowance
- prorated base salary
- no active salary record
- overlapping salary record
- salary gap
- holiday overlaps weekend
- leave overlaps holiday
- zero denominator guard
- preview then finalize with frozen snapshot

## 20. Practical Recommendation

The best implementation path for this codebase is:

- keep `EmployeeSalary` as the salary agreement source
- keep `DailyWork` as the attendance source
- use `CompanyCalendar` and `CalendarDate` for day classification
- strengthen `PayrollPolicy` and `PayRateRule`
- add schedule and adjustment entities
- treat `PayrollRun` as header and `PayrollResult` as line item
- store frozen formula details in `PayrollResultDetail`

That gives you a design that can handle the listed cases without rewriting the current HR and salary modules.


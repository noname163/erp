package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.dto.response.EmployeeDailyWorkListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.repositories.projections.EmployeeDailyWorkListProjection;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeDailyWorkService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

@Service
public class EmployeeDailyWorkServiceImpl extends AbstractAuditableService implements EmployeeDailyWorkService {
    private static final String DEFAULT_SORT_BY = "workingDate";
    private static final String DEFAULT_SORT_DIR = "DESC";

    private final DailyWorkRepository dailyWorkRepository;
    private final UserProfileRepository userProfileRepository;

    public EmployeeDailyWorkServiceImpl(DailyWorkRepository dailyWorkRepository,
            UserProfileRepository userProfileRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.dailyWorkRepository = dailyWorkRepository;
        this.userProfileRepository = userProfileRepository;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public String createEmployeeDailyWorks(List<EmployeeDailyWorkRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_DAILY_WORK_REQUESTS_INVALID);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        List<DailyWork> dailyWorks = new ArrayList<>(requests.size());
        Set<String> dedupeKeys = new HashSet<>();
        for (EmployeeDailyWorkRequest request : requests) {
            if (request == null) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_REQUESTS_INVALID);
            }

            String userProfileCode = CustomStringUtils.normalizeCode(request.getUserProfileCode());
            if (userProfileCode == null) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_USER_PROFILE_CODE_INVALID);
            }

            LocalDate workingDate = request.getWorkingDate();

            String key = userProfileCode + "|" + workingDate;
            if (!dedupeKeys.add(key)) {
                throw new ConflictException(Messages.ERROR_DAILY_WORK_ALREADY_EXISTS);
            }

            UserProfile userProfile = userProfileRepository.findByCode(userProfileCode)
                    .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND));

            String employeeCompanyCode = userProfile.getAccount() == null ? null
                    : userProfile.getAccount().getCompanyCode();
            if (employeeCompanyCode == null || employeeCompanyCode.isBlank()
                    || !companyCode.equals(employeeCompanyCode)) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_EMPLOYEE_COMPANY_MISMATCH);
            }
            if (userProfile.getIsActive() == null || !userProfile.getIsActive()) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_EMPLOYEE_INACTIVE);
            }

            if (dailyWorkRepository.existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(userProfileCode,
                    workingDate)) {
                throw new ConflictException(Messages.ERROR_DAILY_WORK_ALREADY_EXISTS);
            }

            LocalDateTime startDateTime = LocalDateTime.of(workingDate, request.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(workingDate, request.getEndTime());
            if (!endDateTime.isAfter(startDateTime)) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_START_END_TIME_INVALID);
            }

            Integer otTime = request.getOtTime();

            BigDecimal hoursWorked = calculateHoursWorked(startDateTime, endDateTime, otTime);

            DailyWork dailyWork = DailyWork.builder()
                    .userProfile(userProfile)
                    .workingDate(workingDate)
                    .startTime(startDateTime)
                    .endTime(endDateTime)
                    .quantity(request.getQuantity())
                    .unit(request.getUnit())
                    .workType(request.getWorkType())
                    .usedPto(Boolean.TRUE.equals(request.getUsedPto()))
                    .otTime(otTime == null ? 0 : otTime)
                    .hoursWorked(hoursWorked)
                    .build();

            generateCodeIfMissing(dailyWork, CodePrefixes.DAILY_WORK);
            applyInsertAudit(dailyWork);
            dailyWorks.add(dailyWork);
        }

        dailyWorkRepository.saveAll(dailyWorks);

        String firstUserProfileCode = CustomStringUtils.normalizeCode(
                requests.get(0) == null ? null : requests.get(0).getUserProfileCode());
        return String.format(Messages.DAILY_WORK_CREATE_SUCCESS,
                firstUserProfileCode == null ? "" : firstUserProfileCode);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDailyWorkListResponse> getEmployeeDailyWorks(
            String employeeCode,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isPto,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir) {
        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException(Messages.ERROR_DAILY_WORK_WORKING_DATE_INVALID);
        }

        Pageable pageable = PageableUtils.create(
                page,
                size,
                resolveSortBy(sortBy),
                sortDir == null || sortDir.isBlank() ? DEFAULT_SORT_DIR : sortDir);

        Page<EmployeeDailyWorkListProjection> dailyWorks = dailyWorkRepository.findEmployeeDailyWorksByFilters(
                companyCode,
                normalizeText(employeeCode),
                startDate,
                endDate,
                isPto,
                pageable);

        return PageableUtils.mapPage(dailyWorks, this::toListResponse, Messages.SUCCESS);
    }

    private static BigDecimal calculateHoursWorked(LocalDateTime startDateTime, LocalDateTime endDateTime,
            Integer otTime) {
        long minutes = Duration.between(startDateTime, endDateTime).toMinutes();
        BigDecimal baseHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        if (otTime == null || otTime == 0) {
            return baseHours;
        }
        return baseHours.add(BigDecimal.valueOf(otTime));
    }

    private EmployeeDailyWorkListResponse toListResponse(EmployeeDailyWorkListProjection projection) {
        return new EmployeeDailyWorkListResponse(
                projection.getEmployeeCode(),
                blankToNull(projection.getEmployeeName()),
                projection.getLogDay(),
                toLocalTime(projection.getStartTime()),
                toLocalTime(projection.getEndTime()),
                blankToNull(projection.getCreatedByName()),
                blankToNull(projection.getEditedByName()),
                projection.getOtTime(),
                projection.getUsedPto(),
                blankToNull(projection.getWorkType()));
    }

    private static LocalTime toLocalTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalTime();
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return DEFAULT_SORT_BY;
        }

        return switch (sortBy.trim()) {
            case "employeeCode", "code" -> "userProfile.code";
            case "employeeName" -> "userProfile.firstName";
            case "logDay", "workingDate" -> "workingDate";
            case "startTime" -> "startTime";
            case "endTime" -> "endTime";
            case "createdBy" -> "createdBy";
            case "editedBy", "updatedBy" -> "updatedBy";
            case "otTime" -> "otTime";
            case "usedPto", "isPto" -> "usedPto";
            default -> DEFAULT_SORT_BY;
        };
    }
}

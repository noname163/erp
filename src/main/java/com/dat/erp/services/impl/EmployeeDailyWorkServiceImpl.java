package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeDailyWorkRequest;
import com.dat.erp.dto.response.EmployeeDailyWorkListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ForbiddenException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeDailyWorkMapper;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.repositories.projections.EmployeeDailyWorkListProjection;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeeDailyWorkService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

@Service
public class EmployeeDailyWorkServiceImpl extends AbstractAuditableService implements EmployeeDailyWorkService {
    private static final String DEFAULT_SORT_BY = "workingDate";
    private static final String DEFAULT_SORT_DIR = "DESC";

    private final DailyWorkRepository dailyWorkRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmployeeDailyWorkMapper employeeDailyWorkMapper;

    public EmployeeDailyWorkServiceImpl(DailyWorkRepository dailyWorkRepository,
            UserProfileRepository userProfileRepository,
            EmployeeDailyWorkMapper employeeDailyWorkMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.dailyWorkRepository = dailyWorkRepository;
        this.userProfileRepository = userProfileRepository;
        this.employeeDailyWorkMapper = employeeDailyWorkMapper;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public String createEmployeeDailyWorks(List<EmployeeDailyWorkRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_DAILY_WORK_REQUESTS_INVALID);
        }

        String companyCode = requireCurrentUserCompanyCode();
        CustomUserDetails currentUser = securityContextService.getCurrentUser();
        String scopedEmployeeCode = resolveScopedEmployeeCode(currentUser, null);

        Map<EmployeeDailyWorkRequest, String> userProfileCodeByRequest = new LinkedHashMap<>();
        Set<String> requestedUserProfileCodes = new HashSet<>();
        Set<LocalDate> requestedWorkingDates = new HashSet<>();
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
            if (scopedEmployeeCode != null && !scopedEmployeeCode.equals(userProfileCode)) {
                throw new ForbiddenException("AUTH_403_001: Forbidden");
            }

            LocalDate workingDate = request.getWorkingDate();

            String key = userProfileCode + "|" + workingDate;
            if (!dedupeKeys.add(key)) {
                throw new ConflictException(Messages.ERROR_DAILY_WORK_ALREADY_EXISTS);
            }
            userProfileCodeByRequest.put(request, userProfileCode);
            requestedUserProfileCodes.add(userProfileCode);
            if (workingDate != null) {
                requestedWorkingDates.add(workingDate);
            }
        }

        Map<String, UserProfile> userProfileByCode = userProfileRepository
                .findAllByCodeInAndIsDeletedFalseWithAccount(requestedUserProfileCodes)
                .stream()
                .collect(java.util.stream.Collectors.toMap(UserProfile::getCode, profile -> profile));
        if (userProfileByCode.size() != requestedUserProfileCodes.size()) {
            throw new ResourceNotFoundException(Messages.ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND);
        }

        Set<String> existingDailyWorkKeys = new HashSet<>();
        if (!requestedWorkingDates.isEmpty()) {
            dailyWorkRepository.findExistingByUserProfileCodesAndWorkingDates(requestedUserProfileCodes, requestedWorkingDates)
                    .forEach(existingDailyWork -> {
                        UserProfile userProfile = existingDailyWork.getUserProfile();
                        if (userProfile != null
                                && userProfile.getCode() != null
                                && existingDailyWork.getWorkingDate() != null) {
                            existingDailyWorkKeys.add(userProfile.getCode() + "|" + existingDailyWork.getWorkingDate());
                        }
                    });
        }

        for (EmployeeDailyWorkRequest request : requests) {
            String userProfileCode = userProfileCodeByRequest.get(request);
            UserProfile userProfile = userProfileByCode.get(userProfileCode);
            if (userProfile == null) {
                throw new ResourceNotFoundException(Messages.ERROR_DAILY_WORK_EMPLOYEE_NOT_FOUND);
            }

            String employeeCompanyCode = userProfile.getAccount() == null ? null
                    : userProfile.getAccount().getCompanyCode();
            if (employeeCompanyCode == null || employeeCompanyCode.isBlank()
                    || !companyCode.equals(employeeCompanyCode)) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_EMPLOYEE_COMPANY_MISMATCH);
            }
            if (userProfile.getIsActive() == null || !userProfile.getIsActive()) {
                throw new BadRequestException(Messages.ERROR_DAILY_WORK_EMPLOYEE_INACTIVE);
            }

            LocalDate workingDate = request.getWorkingDate();
            if (existingDailyWorkKeys.contains(userProfileCode + "|" + workingDate)) {
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
        String companyCode = requireCurrentUserCompanyCode();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException(Messages.ERROR_DAILY_WORK_WORKING_DATE_INVALID);
        }
        String scopedEmployeeCode = resolveScopedEmployeeCode(securityContextService.getCurrentUser(), employeeCode);

        Pageable pageable = PageableUtils.create(
                page,
                size,
                resolveSortBy(sortBy),
                sortDir == null || sortDir.isBlank() ? DEFAULT_SORT_DIR : sortDir);

        Page<EmployeeDailyWorkListProjection> dailyWorks = dailyWorkRepository.findEmployeeDailyWorksByFilters(
                companyCode,
                scopedEmployeeCode,
                startDate,
                endDate,
                isPto,
                pageable);

        return PageableUtils.mapPage(dailyWorks, employeeDailyWorkMapper::toListResponse, Messages.SUCCESS);
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

    private String resolveScopedEmployeeCode(CustomUserDetails currentUser, String requestedEmployeeCode) {
        if (!isEmployee(currentUser)) {
            return CustomStringUtils.trimToNull(CustomStringUtils.normalizeCode(requestedEmployeeCode));
        }

        UserProfile currentProfile = currentUser.getUserProfile();
        String currentEmployeeCode = currentProfile == null ? null : CustomStringUtils.normalizeCode(currentProfile.getCode());
        if (currentEmployeeCode == null) {
            throw new ForbiddenException("AUTH_403_001: Forbidden");
        }

        String normalizedRequestedEmployeeCode = CustomStringUtils.normalizeCode(requestedEmployeeCode);
        if (normalizedRequestedEmployeeCode != null && !currentEmployeeCode.equals(normalizedRequestedEmployeeCode)) {
            throw new ForbiddenException("AUTH_403_001: Forbidden");
        }

        return currentEmployeeCode;
    }

    private boolean isEmployee(CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getAccount() == null || currentUser.getAccount().getRole() == null) {
            return false;
        }
        String roleName = currentUser.getAccount().getRole().getName();
        return roleName != null && "EMPLOYEE".equalsIgnoreCase(roleName.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<DailyWorkForSalaryResponse>> getEmployeeDailyWorksByEmployeeCodes(
            List<String> employeeCodes) {
        if (employeeCodes == null || employeeCodes.isEmpty()) {
            return Map.of();
        }

        String companyCode = requireCurrentUserCompanyCode();

        List<String> normalizedEmployeeCodes = employeeCodes.stream()
                .map(CustomStringUtils::normalizeCode)
                .filter(code -> code != null)
                .distinct()
                .toList();
        if (normalizedEmployeeCodes.isEmpty()) {
            return Map.of();
        }

        Map<String, Map<DayType, BigDecimal>> aggregatedHoursByEmployeeCode = new LinkedHashMap<>();
        normalizedEmployeeCodes
                .forEach(employeeCode -> aggregatedHoursByEmployeeCode.put(employeeCode, new LinkedHashMap<>()));

        List<DailyWork> dailyWorks = dailyWorkRepository.findAllForSalaryByCompanyCodeAndEmployeeCodes(companyCode,
                normalizedEmployeeCodes);
        for (DailyWork dailyWork : dailyWorks) {
            UserProfile userProfile = dailyWork.getUserProfile();
            if (userProfile == null || userProfile.getCode() == null) {
                continue;
            }

            Map<DayType, BigDecimal> employeeDailyWorks = aggregatedHoursByEmployeeCode.get(userProfile.getCode());
            if (employeeDailyWorks == null || dailyWork.getWorkType() == null) {
                continue;
            }

            employeeDailyWorks.merge(
                    dailyWork.getWorkType(),
                    dailyWork.getHoursWorked() == null ? BigDecimal.ZERO : dailyWork.getHoursWorked(),
                    BigDecimal::add);
        }

        Map<String, List<DailyWorkForSalaryResponse>> dailyWorksByEmployeeCode = new LinkedHashMap<>();
        aggregatedHoursByEmployeeCode.forEach((employeeCode, hoursByDayType) -> dailyWorksByEmployeeCode.put(
                employeeCode,
                hoursByDayType.entrySet().stream()
                        .map(entry -> employeeDailyWorkMapper.toSalaryResponse(entry.getKey(), entry.getValue()))
                        .toList()));

        return dailyWorksByEmployeeCode;
    }
}

package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.payroll.WorkScheduleDetailRequest;
import com.dat.erp.dto.request.payroll.WorkScheduleRequest;
import com.dat.erp.dto.response.payroll.WorkScheduleResponse;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.entities.WorkScheduleDetail;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.WorkScheduleDetailRepository;
import com.dat.erp.repositories.customrepositories.WorkScheduleRepository;
import com.dat.erp.services.WorkScheduleService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class WorkScheduleServiceImpl extends AbstractAuditableService implements WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final WorkScheduleDetailRepository workScheduleDetailRepository;

    public WorkScheduleServiceImpl(
            WorkScheduleRepository workScheduleRepository,
            WorkScheduleDetailRepository workScheduleDetailRepository) {
        this.workScheduleRepository = workScheduleRepository;
        this.workScheduleDetailRepository = workScheduleDetailRepository;
    }

    @Override
    @Transactional
    public WorkScheduleResponse create(WorkScheduleRequest request) {
        validateRequest(request);
        WorkSchedule schedule = new WorkSchedule();
        mapSchedule(schedule, request);
        generateCodeIfMissing(schedule, CodePrefixes.WORK_SCHEDULE);
        applyInsertAudit(schedule);
        WorkSchedule savedSchedule = workScheduleRepository.save(schedule);
        List<WorkScheduleDetail> details = saveDetails(savedSchedule, request.details());
        return toResponse(savedSchedule, details);
    }

    @Override
    @Transactional
    public WorkScheduleResponse update(String code, WorkScheduleRequest request) {
        validateRequest(request);
        WorkSchedule schedule = findOwnedSchedule(code);
        mapSchedule(schedule, request);
        applyUpdateAudit(schedule);
        WorkSchedule savedSchedule = workScheduleRepository.save(schedule);
        softDeleteDetails(savedSchedule.getCode());
        List<WorkScheduleDetail> details = saveDetails(savedSchedule, request.details());
        return toResponse(savedSchedule, details);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkScheduleResponse get(String code) {
        WorkSchedule schedule = findOwnedSchedule(code);
        return toResponse(schedule, findDetails(schedule.getCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> list() {
        String companyCode = resolveCurrentUserCompanyCode();
        return workScheduleRepository.findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(companyCode).stream()
                .map(schedule -> toResponse(schedule, findDetails(schedule.getCode())))
                .toList();
    }

    @Override
    @Transactional
    public void delete(String code) {
        WorkSchedule schedule = findOwnedSchedule(code);
        schedule.setIsDeleted(true);
        applyUpdateAudit(schedule);
        workScheduleRepository.save(schedule);
        softDeleteDetails(schedule.getCode());
    }

    private void validateRequest(WorkScheduleRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BadRequestException(Messages.ERROR_WORK_SCHEDULE_NAME_INVALID);
        }
        if (request.effectiveFrom() == null || request.effectiveTo() == null || request.effectiveFrom().isAfter(request.effectiveTo())) {
            throw new BadRequestException(Messages.ERROR_WORK_SCHEDULE_DETAILS_INVALID);
        }
        if (request.details() == null || request.details().isEmpty()) {
            throw new BadRequestException(Messages.ERROR_WORK_SCHEDULE_DETAILS_INVALID);
        }
        Set<Integer> days = new HashSet<>();
        for (WorkScheduleDetailRequest detail : request.details()) {
            if (detail == null || detail.dayOfWeek() == null || detail.dayOfWeek() < 1 || detail.dayOfWeek() > 7
                    || !days.add(detail.dayOfWeek())) {
                throw new BadRequestException(Messages.ERROR_WORK_SCHEDULE_DETAILS_INVALID);
            }
        }
    }

    private WorkSchedule findOwnedSchedule(String code) {
        WorkSchedule schedule = workScheduleRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_WORK_SCHEDULE_NOT_FOUND));
        if (!Objects.equals(schedule.getCompanyCode(), resolveCurrentUserCompanyCode())) {
            throw new ResourceNotFoundException(Messages.ERROR_WORK_SCHEDULE_NOT_FOUND);
        }
        return schedule;
    }

    private void mapSchedule(WorkSchedule schedule, WorkScheduleRequest request) {
        schedule.setName(request.name().trim());
        schedule.setDescription(request.description());
        schedule.setScheduleType(request.scheduleType());
        schedule.setStandardHoursPerDay(request.standardHoursPerDay());
        schedule.setStandardMinutesPerDay(request.standardMinutesPerDay());
        schedule.setFlexibleWorkingHours(request.flexibleWorkingHours());
        schedule.setCrossMidnightAllowed(request.crossMidnightAllowed());
        schedule.setEffectiveFrom(request.effectiveFrom());
        schedule.setEffectiveTo(request.effectiveTo());
    }

    private List<WorkScheduleDetail> saveDetails(WorkSchedule schedule, List<WorkScheduleDetailRequest> requests) {
        List<WorkScheduleDetail> details = new ArrayList<>();
        for (WorkScheduleDetailRequest request : requests) {
            WorkScheduleDetail detail = new WorkScheduleDetail();
            detail.setWorkSchedule(schedule);
            detail.setDayOfWeek(request.dayOfWeek());
            detail.setIsWorkingDay(request.isWorkingDay());
            detail.setStartTime(request.startTime());
            detail.setEndTime(request.endTime());
            detail.setBreakMinutes(request.breakMinutes());
            detail.setPaidBreak(request.paidBreak());
            detail.setFullDayThresholdMinutes(request.fullDayThresholdMinutes());
            generateCodeIfMissing(detail, CodePrefixes.WORK_SCHEDULE_DETAIL);
            applyInsertAudit(detail);
            details.add(detail);
        }
        return workScheduleDetailRepository.saveAll(details);
    }

    private void softDeleteDetails(String scheduleCode) {
        List<WorkScheduleDetail> details = findDetails(scheduleCode);
        if (details.isEmpty()) {
            return;
        }
        for (WorkScheduleDetail detail : details) {
            detail.setIsDeleted(true);
            applyUpdateAudit(detail);
        }
        workScheduleDetailRepository.saveAll(details);
    }

    private List<WorkScheduleDetail> findDetails(String scheduleCode) {
        return workScheduleDetailRepository.findByWorkSchedule_CodeAndIsDeletedFalseOrderByDayOfWeekAsc(scheduleCode);
    }

    private WorkScheduleResponse toResponse(WorkSchedule schedule, List<WorkScheduleDetail> details) {
        return new WorkScheduleResponse(
                schedule.getCode(),
                schedule.getName(),
                schedule.getDescription(),
                schedule.getScheduleType(),
                schedule.getStandardHoursPerDay(),
                schedule.getStandardMinutesPerDay(),
                schedule.getFlexibleWorkingHours(),
                schedule.getCrossMidnightAllowed(),
                schedule.getEffectiveFrom(),
                schedule.getEffectiveTo(),
                details.stream()
                        .map(detail -> new WorkScheduleDetailRequest(detail.getDayOfWeek(), detail.getIsWorkingDay(),
                                detail.getStartTime(), detail.getEndTime(), detail.getBreakMinutes(), detail.getPaidBreak(),
                                detail.getFullDayThresholdMinutes()))
                        .toList());
    }
}

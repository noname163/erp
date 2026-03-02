package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.CommonStatus;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.dto.response.department.DepartmentResponse;
import com.dat.erp.entities.Department;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.DepartmentMapper;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.services.DepartmentService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.PageableUtils;

@Service
public class DepartmentServiceImpl extends AbstractAuditableService implements DepartmentService {
    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public String createDepartment(DepartmentRequest departmentRequest) {
        Department department = setAuditDepartmentInfo(departmentRequest);
        departmentRepository.save(department);
        return department.getCode();
    }

    @Override
    public String createDefaultDepartment(DepartmentRequest departmentRequest, String actorCode) {
        if (departmentRequest == null) {
            throw new IllegalArgumentException("DepartmentRequest cannot be null");
        }
        String name = departmentRequest.getName();
        String companyCode = departmentRequest.getCompanyCode();
        Department existing = departmentRepository.findByNameAndCompanyCode(name, companyCode).orElse(null);
        if (existing != null) {
            log.info(
                    "AUDIT action=CREATE_DEFAULT_DEPARTMENT actor={} companyCode={} result=ALREADY_EXISTS name={} departmentCode={}",
                    actorCode, companyCode, name, existing.getCode());
            return existing.getCode();
        }
        Department department = setAuditDepartmentInfo(departmentRequest);
        department.setCompanyCode(departmentRequest.getCompanyCode());
        departmentRepository.save(department);
        String code = department.getCode();
        log.info(
                "AUDIT action=CREATE_DEFAULT_DEPARTMENT actor={} companyCode={} result=SUCCESS name={} departmentCode={}",
                actorCode, companyCode, name, code);
        return code;
    }

    @Override
    public PagedResponse<DepartmentResponse> getDepartmentByCompanyCode(String searchKey, String searchValue,
            Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Department> data = departmentRepository.findAll(pageable);
        return PageableUtils.mapPage(data, departmentMapper::toResponse, Messages.SUCCESS);
    }

    @Override
    public List<SelectionOptionResponse> getDepartmentOptionsByCompanyCode(String name) {
        String companyCode = resolveCurrentUserCompanyCode();
        List<Department> departments = departmentRepository
                .findByNameAndCompanyCodeAndStatusAndIsDeletedFalseOrderByNameAsc(name, companyCode, CommonStatus.ACTIVATE);
        List<SelectionOptionResponse> options = new ArrayList<>(departments.size());
        for (Department department : departments) {
            SelectionOptionResponse option = new SelectionOptionResponse();
            option.setCode(department.getCode());
            option.setName(department.getName());
            options.add(option);
        }
        return options;
    }

    private Department setAuditDepartmentInfo(DepartmentRequest departmentRequest) {
        Department department = departmentMapper.toEntity(departmentRequest);
        if (department.getName() != null
                && departmentRepository.existsByNameAndCompanyCode(department.getName(), department.getCompanyCode())) {
            throw new ConflictException(Messages.ERROR_DEPARTMENT_NAME_EXISTS);
        }
        generateCodeIfMissing(department, CodePrefixes.DEPARTMENT);
        applyInsertAudit(department);
        department.setStatus(CommonStatus.ACTIVATE);
        return department;
    }
}

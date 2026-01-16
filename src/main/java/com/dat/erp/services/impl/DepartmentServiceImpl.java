package com.dat.erp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.CommonStatus;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.DepartmentRequest;
import com.dat.erp.dto.response.DepartmentResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.Department;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.DepartmentMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.services.DepartmentService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.utils.PageableUtils;

@Service
public class DepartmentServiceImpl extends AbstractAuditableService implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public String createDepartment(DepartmentRequest departmentRequest) {
        Department department = departmentMapper.toEntity(departmentRequest);
        Company company = companyRepository.findByCode(departmentRequest.getCompanyCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_COMPANY_NOT_FOUND_WITH_CODE, departmentRequest.getCompanyCode())));
        if (department.getName() != null
                && departmentRepository.existsByNameAndCompany(department.getName(), company)) {
            throw new ConflictException(Messages.ERROR_DEPARTMENT_NAME_EXISTS);
        }
        generateCodeIfMissing(department, CodePrefixes.DEPARTMENT);
        applyInsertAudit(department);
        department.setCompany(company);
        department.setStatus(CommonStatus.ACTIVATE);
        departmentRepository.save(department);
        return department.getCode();
    }

    @Override
    public PagedResponse<DepartmentResponse> getDepartmentByCompanyCode(String searchKey, String searchValue,
            Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<Department> data = departmentRepository.findAll(pageable);
        return PageableUtils.mapPage(data, departmentMapper::toResponse, Messages.SUCCESS);
    }

}

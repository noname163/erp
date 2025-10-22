package com.dat.erp.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationDetailResponse;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeMapper;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.repositories.specifications.EmployeeSpecifications;
import com.dat.erp.services.EmployeeInformationService;
import com.dat.erp.utils.PageableUtils;

@Service
public class EmployeeInformationServiceImpl implements EmployeeInformationService {
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public String createEmployeeInformation(EmployeeInformationRequest request) {
        employeeInformationRepository.findByEmail(request.getEmail()).ifPresent(e -> {
            throw new ConflictException(Messages.ERROR_EMPLOYEE_EMAIL_EXISTS);
        });
        EmployeeInformation employeeInformation = employeeMapper.toEntity(request);
        employeeInformationRepository.save(employeeInformation);
        return employeeInformation.getCode();
    }

    @Override
    public PagedResponse<EmployeeInformationResponse> getListEmployeeInformationResponse(String companyCode,
            String deparmentCode, String managerCode, String keyword,
            Integer page, Integer pageSize, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, pageSize, sortBy, sortDir);
        Specification<EmployeeInformation> employeeSpecification = EmployeeSpecifications.build(deparmentCode,
                companyCode, managerCode, keyword);
        Page<EmployeeInformation> data = employeeInformationRepository.findAll(employeeSpecification, pageable);
        return PageableUtils.mapPage(data, employeeMapper::toResponse, Messages.SUCCESS);
    }

    @Override
    public EmployeeInformationDetailResponse employeeInformationDetailResponse(String code) {
        EmployeeInformation employeeInformation = employeeInformationRepository.findDetailedByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_EMPLOYEE_NOT_FOUND_WITH_CODE, code)));
        return employeeMapper.toResponseDetail(employeeInformation);
    }

    @Override
    public String updateEmployeeInformation(EmployeeInformationRequest request, String code) {
        EmployeeInformation existing = employeeInformationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.ERROR_EMPLOYEE_NOT_FOUND_WITH_CODE_ALT, code)));

        employeeMapper.updateEmployeeFromDto(request, existing);

        employeeInformationRepository.save(existing);
        return existing.getCode();
    }

    @Override
    public String deleteEmployeeByCode(String code) {
        employeeInformationRepository.deleteByCode(code);
        return code;
    }

    @Override
    public List<String> deleteEmployeeByCodes(List<String> codes) {
        employeeInformationRepository.deleteByCodes(codes);
        return codes;
    }

}

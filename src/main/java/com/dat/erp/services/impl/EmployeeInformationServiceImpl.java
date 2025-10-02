package com.dat.erp.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationDetailResponse;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeMapper;
import com.dat.erp.repositories.customrepositories.EmployeeInformationRepository;
import com.dat.erp.services.EmployeeInformationService;
import com.dat.erp.utils.PageableUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeInformationServiceImpl implements EmployeeInformationService {
    @Autowired
    private EmployeeInformationRepository employeeInformationRepository;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public String createEmployeeInformation(EmployeeInformationRequest request) {
        EmployeeInformation employeeInformation = employeeMapper.toEntity(request);
        employeeInformation.setCode("EMPI-" + UUID.randomUUID());
        employeeInformation
                .setPassword(request.getEmail() + employeeInformation.getUser().getDateOfBirth().replace("-", ""));
        employeeInformation.setEmploymentStatus(CommonStatus.ACTIVATE);
        employeeInformationRepository.save(employeeInformation);
        return employeeInformation.getCode();
    }

    @Override
    public PagedResponse<EmployeeInformationResponse> getListEmployeeInformationResponse(String searchKey,
            String searchValue, Integer page, Integer pageSize, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.create(page, pageSize, sortBy, sortDir);
        Page<EmployeeInformation> data = employeeInformationRepository.findAll(pageable);
        return PageableUtils.mapPage(data, employeeMapper::toResponse, "Success");
    }

    @Override
    public EmployeeInformationDetailResponse employeeInformationDetailResponse(String code) {
        EmployeeInformation employeeInformation = employeeInformationRepository.findDetailedByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Not existed employee with code " + code));
        return employeeMapper.toResponseDetail(employeeInformation);
    }

    @Override
    public String updateEmployeeInformation(EmployeeInformationRequest request, String code) {
        EmployeeInformation existing = employeeInformationRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee with code " + code + " not found"));

        employeeMapper.updateEmployeeFromDto(request, existing);

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

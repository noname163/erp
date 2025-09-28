package com.dat.erp.mapper.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dat.erp.dto.request.EmployeeInformationRequest;
import com.dat.erp.dto.response.EmployeeInformationResponse;
import com.dat.erp.entities.Company;
import com.dat.erp.entities.Department;
import com.dat.erp.entities.EmployeeInformation;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserInformation;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.EmployeeMapper;
import com.dat.erp.repositories.customrepositories.CompanyRepository;
import com.dat.erp.repositories.customrepositories.DepartmentRepository;
import com.dat.erp.repositories.customrepositories.RoleRepository;
import com.dat.erp.repositories.customrepositories.UserInformationRepository;

@Component
public abstract class EmployeeMapperDecorator implements EmployeeMapper {

    @Autowired
    private EmployeeMapper delegate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserInformationRepository userInformationRepository;

    @Override
    public EmployeeInformation toEntity(EmployeeInformationRequest request) {
        EmployeeInformation employeeInformation = delegate.toEntity(request);

        Role role = roleRepository.findByCode(request.getRoleCode())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Unable to find role with code " + request.getRoleCode()));

        Company company = companyRepository.findByCode(request.getCompanyCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unable to find company with code " + request.getCompanyCode()));

        Department department = departmentRepository.findByCode(request.getDepartmentCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unable to find department with code " + request.getDepartmentCode()));

        UserInformation user = userInformationRepository.findByCode(request.getUserCode())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Unable to find user with code " + request.getUserCode()));

        employeeInformation.setRole(role);
        employeeInformation.setCompany(company);
        employeeInformation.setDepartment(department);
        employeeInformation.setUser(user);

        return employeeInformation;
    }

    @Override
    public EmployeeInformationResponse toResponse(EmployeeInformation entity) {
        return delegate.toResponse(entity);
    }
}

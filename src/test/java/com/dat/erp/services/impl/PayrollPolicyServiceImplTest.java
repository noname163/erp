package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.PayrollPolicyRequest;
import com.dat.erp.dto.response.PayrollPolicyResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.mapper.interfaces.PayrollPolicyMapper;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.impl.PayrollPolicyServiceImpl;
import com.dat.erp.systemconfigs.CustomUserDetails;

class PayrollPolicyServiceImplTest {

    @Mock
    private PayrollPolicyRepository payrollPolicyRepository;

    @Mock
    private SystemUnitRepository systemUnitRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @Spy
    private PayrollPolicyMapper payrollPolicyMapper = Mappers.getMapper(PayrollPolicyMapper.class);

    @InjectMocks
    private PayrollPolicyServiceImpl payrollPolicyService;

    private PayrollPolicyRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new PayrollPolicyRequest();
        request.setName("Office Hour Policy");
        request.setStandardQuantityPerDay(8);
        request.setUnitCode("UNT-001");
        request.setStandardStartTime(LocalTime.of(9, 0));
        request.setStandardEndTime(LocalTime.of(18, 0));
        request.setRoundingRule("ROUND_HALF_UP");
        request.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        request.setEffectiveTo(LocalDate.of(2026, 12, 31));

        Account account = new Account();
        account.setCode("ACC-001");
        account.setCompanyCode("CMP-001");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
    }

    @Test
    void createPayrollPolicy_success() {
        SystemUnit unit = new SystemUnit();
        unit.setCode("UNT-001");

        when(payrollPolicyRepository.existsOverlappingByNameAndCompanyCode("Office Hour Policy", "CMP-001",
                request.getEffectiveFrom(), request.getEffectiveTo())).thenReturn(false);
        when(systemUnitRepository.findByCodeAndIsDeletedFalse("UNT-001")).thenReturn(Optional.of(unit));
        when(codeGenerator.nextCode("PPL-")).thenReturn("PPL-000001");
        when(payrollPolicyRepository.save(any(PayrollPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollPolicyResponse response = payrollPolicyService.createPayrollPolicy(request);

        assertNotNull(response);
        assertEquals("PPL-000001", response.getCode());
        assertEquals("Office Hour Policy", response.getName());
        assertEquals("UNT-001", response.getUnitCode());

        ArgumentCaptor<PayrollPolicy> captor = ArgumentCaptor.forClass(PayrollPolicy.class);
        verify(payrollPolicyRepository).save(captor.capture());
        assertEquals("Office Hour Policy", captor.getValue().getName());
        assertEquals("UNT-001", captor.getValue().getUnit().getCode());
    }

    @Test
    void createPayrollPolicy_conflictWhenNameOverlaps() {
        when(payrollPolicyRepository.existsOverlappingByNameAndCompanyCode("Office Hour Policy", "CMP-001",
                request.getEffectiveFrom(), request.getEffectiveTo())).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> payrollPolicyService.createPayrollPolicy(request));

        assertEquals(Messages.ERROR_PAYROLL_POLICY_NAME_EXISTS, ex.getMessage());
        verify(payrollPolicyRepository, never()).save(any(PayrollPolicy.class));
    }

    @Test
    void createPayrollPolicy_badRequestWhenDateRangeInvalid() {
        request.setEffectiveFrom(LocalDate.of(2026, 12, 31));
        request.setEffectiveTo(LocalDate.of(2026, 1, 1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> payrollPolicyService.createPayrollPolicy(request));

        assertEquals(Messages.ERROR_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID, ex.getMessage());
        verify(payrollPolicyRepository, never()).save(any(PayrollPolicy.class));
    }

    @Test
    void createPayrollPolicy_badRequestWhenUnitCodeMissingForQuantity() {
        request.setUnitCode(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> payrollPolicyService.createPayrollPolicy(request));

        assertEquals(Messages.ERROR_PAYROLL_POLICY_UNIT_CODE_INVALID, ex.getMessage());
        verify(payrollPolicyRepository, never()).save(any(PayrollPolicy.class));
    }

    @Test
    void createPayrollPolicy_badRequestWhenStandardTimesInvalid() {
        request.setStandardEndTime(LocalTime.of(8, 59));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> payrollPolicyService.createPayrollPolicy(request));

        assertEquals(Messages.ERROR_PAYROLL_POLICY_STANDARD_TIME_INVALID, ex.getMessage());
        verify(payrollPolicyRepository, never()).save(any(PayrollPolicy.class));
    }

    @Test
    void getPayrollPolicies_returnsFilteredResults() {
        SystemUnit unit = new SystemUnit();
        unit.setCode("UNT-001");

        PayrollPolicy payrollPolicy = PayrollPolicy.builder()
                .name("Office Hour Policy")
                .standardQuantityPerDay(8)
                .unit(unit)
                .standardStartTime(LocalTime.of(9, 0))
                .standardEndTime(LocalTime.of(18, 0))
                .roundingRule("ROUND_HALF_UP")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .build();
        payrollPolicy.setCode("PPL-000001");

        when(payrollPolicyRepository.findByFilters("CMP-001", "Office", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), "UNT-001"))
                .thenReturn(List.of(payrollPolicy));

        List<PayrollPolicyResponse> responses = payrollPolicyService.getPayrollPolicies(" Office ",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), " UNT-001 ");

        assertEquals(1, responses.size());
        assertEquals("PPL-000001", responses.get(0).getCode());
        assertEquals("UNT-001", responses.get(0).getUnitCode());
    }

    @Test
    void getPayrollPolicies_badRequestWhenDateRangeInvalid() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> payrollPolicyService.getPayrollPolicies(null, LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 1), null));

        assertEquals(Messages.ERROR_PAYROLL_POLICY_EFFECTIVE_DATES_INVALID, ex.getMessage());
    }
}

package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.SystemUnit;
import com.dat.erp.repositories.customrepositories.SystemUnitRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class SystemUnitServiceImplTest {

    @Mock
    private SystemUnitRepository systemUnitRepository;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private SystemUnitServiceImpl systemUnitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSystemUnitOptionsByCompanyCode_success() {
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        SystemUnit unit = new SystemUnit();
        unit.setCode("UNT-000001");
        unit.setName("DAY");

        when(systemUnitRepository.findOptionsByFilters(eq("CMP-1"), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(unit), PageRequest.of(0, 20), 1));

        PagedResponse<SelectionOptionResponse> result = systemUnitService.getSystemUnitOptionsByCompanyCode(null,null, 0, 20,
                null, "ASC");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("UNT-000001", result.getData().get(0).getCode());
        assertEquals("DAY", result.getData().get(0).getName());
        assertEquals(Messages.SUCCESS, result.getMessage());
        verify(systemUnitRepository).findOptionsByFilters(eq("CMP-1"), isNull(), any());
    }
}

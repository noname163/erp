package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.SystemApiResponse;
import com.dat.erp.entities.SystemApi;
import com.dat.erp.mapper.interfaces.SystemApiMapper;
import com.dat.erp.repositories.customrepositories.SystemApiRepository;

class SystemApiServiceImplTest {

    @Mock
    private SystemApiRepository systemApiRepository;

    @Mock
    private SystemApiMapper systemApiMapper;

    @InjectMocks
    private SystemApiServiceImpl systemApiService;

    private SystemApi entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        entity = new SystemApi();
        entity.setCode("API-1");
    }

    @Test
    void testGetListSystemApi_Success() {
        // Arrange
        Page<SystemApi> page = new PageImpl<>(Collections.singletonList(entity));
        when(systemApiRepository.findAll(any(Pageable.class))).thenReturn(page);

        SystemApiResponse response = new SystemApiResponse();
        response.setCode("API-1");
        when(systemApiMapper.toResponse(entity)).thenReturn(response);

        // Act
        PagedResponse<SystemApiResponse> result = systemApiService.getListSystemApi("search", 0, 10, "id", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertSame(response, result.getData().get(0));
        verify(systemApiRepository).findAll(any(Pageable.class));
        verify(systemApiMapper).toResponse(entity);
    }
}

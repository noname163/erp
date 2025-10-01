package com.dat.erp.services.impl;

import com.dat.erp.dto.request.UserInformationRequest;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.UserInformationResponse;
import com.dat.erp.entities.UserInformation;
import com.dat.erp.mapper.interfaces.UserMapper;
import com.dat.erp.repositories.customrepositories.UserInformationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserInformationServiceImplTest {

    @Mock
    private UserInformationRepository userInformationRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserInformationServiceImpl userInformationService;

    private UserInformationRequest request;
    private UserInformation userInformation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new UserInformationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");

        userInformation = new UserInformation();
        userInformation.setFirstName("John");
        userInformation.setLastName("Doe");
    }

    // ------------------------------
    // createUserInformation
    // ------------------------------
    @Test
    void testCreateUserInformation_Success() {
        when(userMapper.toEntity(request)).thenReturn(userInformation);

        String code = userInformationService.createUserInformation(request);

        assertNotNull(code);
        assertTrue(code.startsWith("USR-"));
        verify(userInformationRepository).save(userInformation);
    }

    // ------------------------------
    // getListUserInformation
    // ------------------------------
    @Test
    void testGetListUserInformation_Success() {
        Page<UserInformation> page = new PageImpl<>(Collections.singletonList(userInformation));
        when(userInformationRepository.findAll(any(Pageable.class))).thenReturn(page);

        UserInformationResponse response = new UserInformationResponse();
        when(userMapper.toResponse(userInformation)).thenReturn(response);

        PagedResponse<UserInformationResponse> result = userInformationService.getListUserInformation("key", "value",
                10, 0, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertSame(response, result.getData().get(0));
    }
}

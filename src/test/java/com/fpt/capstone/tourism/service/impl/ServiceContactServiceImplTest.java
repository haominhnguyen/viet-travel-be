package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.ServiceContactManagementRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceContactManagementResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceContactMapper;
import com.fpt.capstone.tourism.model.ServiceContact;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.repository.ServiceContactRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceContactServiceImplTest {

    @Mock
    private ServiceContactRepository serviceContactRepository;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private ServiceContactMapper serviceContactMapper;

    @InjectMocks
    private ServiceContactServiceImpl serviceContactService;

    private ServiceContact mockServiceContact;
    private ServiceContactManagementRequestDTO mockRequestDTO;
    private ServiceContactManagementResponseDTO mockResponseDTO;
    private ServiceProvider mockServiceProvider;

    @BeforeEach
    void setUp() {
        mockServiceProvider = new ServiceProvider();
        mockServiceProvider.setId(1L);
        mockServiceProvider.setName("Test Provider");

        mockServiceContact = new ServiceContact();
        mockServiceContact.setId(1L);
        mockServiceContact.setFullName("John Doe");
        mockServiceContact.setPhoneNumber("1234567890");
        mockServiceContact.setEmail("john@example.com");
        mockServiceContact.setServiceProvider(mockServiceProvider);

        mockRequestDTO = new ServiceContactManagementRequestDTO();
        mockRequestDTO.setFullName("John Doe");
        mockRequestDTO.setPhoneNumber("1234567890");
        mockRequestDTO.setEmail("john@example.com");
        mockRequestDTO.setPosition("Manager");

        mockResponseDTO = new ServiceContactManagementResponseDTO();
        mockResponseDTO.setId(1L);
        mockResponseDTO.setFullName("John Doe");
        mockResponseDTO.setPhoneNumber("1234567890");
        mockResponseDTO.setEmail("john@example.com");
        mockResponseDTO.setServiceProviderName("Test Provider");
    }

    @Test
    void createServiceContact_Success() {
        when(serviceContactRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(serviceContactRepository.existsByEmail(anyString())).thenReturn(false);
        when(serviceProviderRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceProvider));
        when(serviceContactMapper.toEntity(any())).thenReturn(mockServiceContact);
        when(serviceContactRepository.save(any())).thenReturn(mockServiceContact);
        when(serviceContactMapper.toResponseDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<ServiceContactManagementResponseDTO> response =
                serviceContactService.createServiceContact(mockRequestDTO, 1L);

        assertEquals(200, response.getStatus());
        assertEquals("John Doe", response.getData().getFullName());
        verify(serviceContactRepository, times(1)).save(any());
    }

    @Test
    void createServiceContact_Fail_DuplicatePhoneNumber() {
        when(serviceContactRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                serviceContactService.createServiceContact(mockRequestDTO, 1L));

        assertEquals(409, exception.getHttpCode());
        assertEquals(DUPLICATE_SERVICE_CONTACT_PHONE, exception.getResponseMessage());
    }

    @Test
    void getServiceContactById_Success() {
        when(serviceContactRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceContact));
        when(serviceContactMapper.toResponseDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<?> response = serviceContactService.getServiceContactById(1L);

        assertEquals(200, response.getStatus());
        assertEquals("John Doe", ((ServiceContactManagementResponseDTO) response.getData()).getFullName());
    }

    @Test
    void getServiceContactById_NotFound() {
        when(serviceContactRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                serviceContactService.getServiceContactById(1L));

        assertEquals(404, exception.getHttpCode());
        assertEquals(SERVICE_CONTACT_NOT_FOUND, exception.getResponseMessage());
    }

    @Test
    void getAllServiceContacts_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<ServiceContact> serviceContactPage = new PageImpl<>(List.of(mockServiceContact), pageable, 1);

        when(serviceContactRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(serviceContactPage);
        when(serviceContactMapper.toResponseDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<PagingDTO<List<ServiceContactManagementResponseDTO>>> response =
                serviceContactService.getAllServiceContacts(0, 10, "", false, "id", "desc", 1L);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotal());
    }

    @Test
    void updateServiceContact_Success() {
        mockServiceContact.setPosition("Manager");
        when(serviceContactRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceContact));
        when(serviceContactRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(serviceContactRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(serviceContactRepository.save(any())).thenReturn(mockServiceContact);
        when(serviceContactMapper.toResponseDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<ServiceContactManagementResponseDTO> response =
                serviceContactService.updateServiceContact(1L, mockRequestDTO, 1L);

        assertEquals(200, response.getStatus());
        assertEquals("John Doe", response.getData().getFullName());
    }

    @Test
    void deleteServiceContact_Success() {
        mockServiceContact.setDeleted(true);
        mockResponseDTO.setDeleted(true);
        when(serviceContactRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceContact));
        when(serviceContactRepository.save(any())).thenReturn(mockServiceContact);
        when(serviceContactMapper.toResponseDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<?> response = serviceContactService.deleteServiceContact(1L, true);

        assertEquals(200, response.getStatus());
        assertTrue(((ServiceContactManagementResponseDTO) response.getData()).getDeleted());
    }
}

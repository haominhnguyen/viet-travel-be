//package com.fpt.capstone.tourism.service.impl;
//
//import com.fpt.capstone.tourism.dto.common.GeneralResponse;
//import com.fpt.capstone.tourism.dto.common.ServiceBaseDTO;
//import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
//import com.fpt.capstone.tourism.dto.response.PagingDTO;
//import com.fpt.capstone.tourism.exception.common.BusinessException;
//import com.fpt.capstone.tourism.mapper.ServiceBaseMapper;
//import com.fpt.capstone.tourism.mapper.custom.ServiceCustomMapper;
//import com.fpt.capstone.tourism.mapper.ServiceFullMapper;
//import com.fpt.capstone.tourism.model.Service;
//import com.fpt.capstone.tourism.model.ServiceProvider;
//import com.fpt.capstone.tourism.repository.ServiceRepository;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.domain.Specification;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ServiceServiceImplTest {
//
//    @Mock
//    private ServiceRepository serviceRepository;
//
//    @Mock
//    private ServiceBaseMapper serviceMapper;
//
//    @Mock
//    private ServiceFullMapper serviceFullMapper;
//
//    @Mock
//    private ServiceCustomMapper serviceCustomMapper;
//
//    @Mock
//    private EntityManager entityManager;
//
//    @InjectMocks
//    private ServiceServiceImpl serviceService;
//
//    private Service service;
//    private ServiceProvider provider;
//    private ServiceFullDTO serviceFullDTO;
//    @BeforeEach
//    void setUp() {
//        provider = new ServiceProvider();
//        provider.setId(1L);
//
//        service = new Service();
//        service.setId(1L);
//        service.setName("Hotel Service");
//        service.setServiceProvider(provider);
//        service.setDeleted(false);
//
//        service.setServiceDetails(new HashSet<>());
//
//        ServiceFullDTO serviceFullDTO = new ServiceFullDTO();
//        serviceFullDTO.setId(service.getId());
//        serviceFullDTO.setName(service.getName());
//    }
//
//    @Test
//    void getAllServices_Success() {
//        int page = 0, size = 10;
//        String keyword = "Hotel";
//        Boolean isDeleted = false;
//        String sortField = "id";
//        String sortDirection = "asc";
//        Long providerId = 1L;
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortField));
//        List<Service> services = List.of(service);
//        Page<Service> servicePage = new PageImpl<>(services, pageable, services.size());
//
//        ServiceBaseDTO serviceDTO = new ServiceBaseDTO();
//        serviceDTO.setId(service.getId());
//        serviceDTO.setName(service.getName());
//
//        when(serviceRepository.findAll((Specification<Service>) any(), eq(pageable))).thenReturn(servicePage);
//        when(serviceMapper.toDTO(service)).thenReturn(serviceDTO);
//
//        GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> response = serviceService.getAllServices(
//                page, size, keyword, isDeleted, sortField, sortDirection, providerId
//        );
//
//        assertNotNull(response);
//        assertNotNull(response.getData());
//        assertEquals(1, response.getData().getItems().size());
//        assertEquals(service.getId(), response.getData().getItems().get(0).getId());
//        assertEquals(service.getName(), response.getData().getItems().get(0).getName());
//
//        verify(serviceRepository).findAll((Specification<Service>) any(), eq(pageable));
//        verify(serviceMapper).toDTO(service);
//    }
//
//
//
//    @Test
//    void getServiceById_NotFound() {
//        when(serviceRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
//
//        BusinessException exception = assertThrows(BusinessException.class, () ->
//                serviceService.getServiceById(1L, 1L)
//        );
//
//        assertEquals(404, exception.getHttpCode());
//        assertEquals("Service not found", exception.getMessage());
//
//        verify(serviceRepository).findByIdWithDetails(1L);
//    }
//
//    @Test
//    void getServiceById_NotBelongToProvider() {
//        ServiceProvider anotherProvider = new ServiceProvider();
//        anotherProvider.setId(2L);
//        service.setServiceProvider(anotherProvider);
//
//        when(serviceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(service));
//
//        BusinessException exception = assertThrows(BusinessException.class, () ->
//                serviceService.getServiceById(1L, 1L)
//        );
//
//        assertEquals(403, exception.getHttpCode());
//        assertEquals("Service does not belong to provider", exception.getMessage());
//
//        verify(serviceRepository).findByIdWithDetails(1L);
//    }
//}

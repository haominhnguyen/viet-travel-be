package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryFullDTO;
import com.fpt.capstone.tourism.dto.request.ServiceCategoryRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceCategoryResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceCategoryDetailMapper;
import com.fpt.capstone.tourism.mapper.ServiceCategoryFullMapper;
import com.fpt.capstone.tourism.mapper.ServiceCategoryMapper;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.repository.ServiceCategoryRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCategoryServiceImplTest {

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private ServiceCategoryFullMapper serviceCategoryFullMapper;

    @Mock
    private ServiceCategoryMapper serviceCategoryMapper;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private ServiceCategoryDetailMapper serviceCategoryDetailMapper;

    @InjectMocks
    private ServiceCategoryServiceImpl serviceCategoryService;

    private ServiceCategory category;
    private ServiceCategoryFullDTO categoryDTO;
    private ServiceCategoryRequestDTO categoryRequestDTO;

    @BeforeEach
    void setUp() {
        category = new ServiceCategory();
        category.setId(1L);
        category.setCategoryName("Hotels");
        category.setDeleted(false);

        categoryDTO = new ServiceCategoryFullDTO();
        categoryDTO.setId(1L);
        categoryDTO.setCategoryName("Hotels");

        categoryRequestDTO = new ServiceCategoryRequestDTO();
        categoryRequestDTO.setCategoryName("Hotels");
    }

    /**
     * Test: Successfully retrieve a service category by ID
     */
    @Test
    void getCategoryById_Success() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(serviceCategoryFullMapper.toDTO(category)).thenReturn(categoryDTO);

        GeneralResponse<ServiceCategoryFullDTO> response = serviceCategoryService.getCategoryById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Hotels", response.getData().getCategoryName());
    }

    /**
     * Test: Service category not found
     */
    @Test
    void getCategoryById_NotFound() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                serviceCategoryService.getCategoryById(1L));

        assertEquals(404, exception.getHttpCode());
    }

    /**
     * Test: Successfully create a new service category
     */
    @Test
    void createCategory_Success() {
        when(serviceCategoryRepository.findByCategoryName("Hotels")).thenReturn(Optional.empty());
        when(serviceCategoryDetailMapper.toEntity(categoryRequestDTO)).thenReturn(category);
        when(serviceCategoryRepository.save(category)).thenReturn(category);
        when(serviceCategoryDetailMapper.toDTO(category)).thenReturn(new ServiceCategoryResponseDTO(1L, "Hotels",false,null,null));

        GeneralResponse<ServiceCategoryResponseDTO> response = serviceCategoryService.createCategory(categoryRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Hotels", response.getData().getCategoryName());
    }

    /**
     * Test: Duplicate category name should fail
     */
    @Test
    void createCategory_CategoryAlreadyExists() {
        when(serviceCategoryRepository.findByCategoryName("Hotels")).thenReturn(Optional.of(category));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                serviceCategoryService.createCategory(categoryRequestDTO));

        assertEquals(409, exception.getHttpCode());
    }

    /**
     * Test: Successfully update an existing service category
     */
    @Test
    void updateCategory_Success() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(serviceCategoryRepository.findByCategoryName("Hotels")).thenReturn(Optional.empty());
        when(serviceCategoryRepository.save(category)).thenReturn(category);
        when(serviceCategoryDetailMapper.toDTO(category)).thenReturn(new ServiceCategoryResponseDTO(1L, "Hotels",false,null,null));

        GeneralResponse<ServiceCategoryResponseDTO> response = serviceCategoryService.updateCategory(1L, categoryRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Hotels", response.getData().getCategoryName());
    }

    /**
     * Test: Updating a non-existent category should fail
     */
    @Test
    void updateCategory_NotFound() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                serviceCategoryService.updateCategory(1L, categoryRequestDTO));

        assertEquals(404, exception.getHttpCode());
    }

    /**
     * Test: Successfully delete (soft delete) a category
     */
    @Test
    void changeCategoryDeletedStatus_Success() {
        ServiceCategory category = new ServiceCategory();
        category.setId(1L);
        category.setCategoryName("Hotels");
        category.setDeleted(false);

        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(serviceCategoryRepository.save(any(ServiceCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(serviceCategoryMapper.toDTO(any(ServiceCategory.class))).thenAnswer(invocation -> {
            ServiceCategory updatedCategory = invocation.getArgument(0);
            return new ServiceCategoryDTO(updatedCategory.getId(), updatedCategory.getCategoryName(), updatedCategory.getDeleted());
        });

        // Act
        GeneralResponse<ServiceCategoryDTO> response = serviceCategoryService.changeCategoryDeletedStatus(1L, true);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData(), "Response data should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getData().getDeleted(), "Deleted status should be true");
    }


    /**
     * Test: Soft deleting a non-existent category should fail
     */
    @Test
    void changeCategoryDeletedStatus_NotFound() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                serviceCategoryService.changeCategoryDeletedStatus(1L, true));

        assertEquals(404, exception.getHttpCode());
    }

    /**
     * Test: Get all categories with pagination
     */
    @Test
    void getAllCategories_Success() {
        Page<ServiceCategory> categoryPage = new PageImpl<>(Collections.singletonList(category));
        when(serviceCategoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(categoryPage);
        when(serviceCategoryFullMapper.toDTO(any(ServiceCategory.class))).thenReturn(categoryDTO);

        GeneralResponse<PagingDTO<List<ServiceCategoryFullDTO>>> response =
                serviceCategoryService.getAllCategories(0, 10, "", false, "id", "asc");

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertFalse(response.getData().getItems().isEmpty());
    }
}


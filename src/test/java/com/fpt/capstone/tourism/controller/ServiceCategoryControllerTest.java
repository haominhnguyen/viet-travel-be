package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryFullDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderSimpleDTO;
import com.fpt.capstone.tourism.dto.request.ServiceCategoryRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceCategoryResponseDTO;
import com.fpt.capstone.tourism.service.ServiceCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceCategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceCategoryService serviceCategoryService;

    @InjectMocks
    private ServiceCategoryController serviceCategoryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(serviceCategoryController).build();

        // Configure ObjectMapper for handling LocalDateTime
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreateCategory() throws Exception {
        // Create request DTO
        ServiceCategoryRequestDTO requestDTO = new ServiceCategoryRequestDTO();
        // Set necessary properties

        // Create expected response
        ServiceCategoryResponseDTO responseDTO = ServiceCategoryResponseDTO.builder()
                .id(1L)
                .categoryName("Hotel")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceCategoryService.createCategory(any(ServiceCategoryRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Service category created successfully", responseDTO));

        mockMvc.perform(post("/admin/service-categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service category created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data.deleted").value(false));
    }

    @Test
    void testGetCategoryDetail() throws Exception {
        // Create service providers list
        List<ServiceProviderSimpleDTO> providers = new ArrayList<>();
        providers.add(ServiceProviderSimpleDTO.builder()
                .id(1L)
                .name("Provider 1")
                .abbreviation("P1")
                .imageUrl("http://example.com/image.jpg")
                .build());

        // Create expected response
        ServiceCategoryFullDTO fullDTO = ServiceCategoryFullDTO.builder()
                .id(1L)
                .categoryName("Hotel")
                .deleted(false)
                .serviceProviders(providers)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceCategoryService.getCategoryById(anyLong()))
                .thenReturn(new GeneralResponse<>(200, "Service category details retrieved successfully", fullDTO));

        mockMvc.perform(get("/admin/service-categories/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service category details retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data.serviceProviders[0].id").value(1))
                .andExpect(jsonPath("$.data.serviceProviders[0].name").value("Provider 1"))
                .andExpect(jsonPath("$.data.serviceProviders[0].abbreviation").value("P1"))
                .andExpect(jsonPath("$.data.serviceProviders[0].imageUrl").value("http://example.com/image.jpg"));
    }

    @Test
    void testUpdateCategory() throws Exception {
        // Create request DTO
        ServiceCategoryRequestDTO requestDTO = new ServiceCategoryRequestDTO();
        // Set necessary properties

        // Create expected response
        ServiceCategoryResponseDTO responseDTO = ServiceCategoryResponseDTO.builder()
                .id(1L)
                .categoryName("Updated Hotel")
                .deleted(false)
                .updatedAt(LocalDateTime.now())
                .build();

        when(serviceCategoryService.updateCategory(eq(1L), any(ServiceCategoryRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Service category updated successfully", responseDTO));

        mockMvc.perform(put("/admin/service-categories/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service category updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Updated Hotel"));
    }

    @Test
    void testChangeStatus() throws Exception {
        // Create expected response
        ServiceCategoryDTO responseDTO = ServiceCategoryDTO.builder()
                .id(1L)
                .categoryName("Hotel")
                .deleted(true)
                .build();

        when(serviceCategoryService.changeCategoryDeletedStatus(eq(1L), eq(true)))
                .thenReturn(new GeneralResponse<>(200, "Service category status changed successfully", responseDTO));

        mockMvc.perform(post("/admin/service-categories/change-status/1")
                        .param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service category status changed successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void testGetCategories() throws Exception {
        // Create category for list
        ServiceCategoryFullDTO categoryDTO = ServiceCategoryFullDTO.builder()
                .id(1L)
                .categoryName("Hotel")
                .deleted(false)
                .serviceProviders(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<ServiceCategoryFullDTO> categories = Collections.singletonList(categoryDTO);
        PagingDTO<List<ServiceCategoryFullDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, categories);

        when(serviceCategoryService.getAllCategories(anyInt(), anyInt(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(new GeneralResponse<>(200, "Service categories retrieved successfully", pagingDTO));

        mockMvc.perform(get("/admin/service-categories/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "hotel")
                        .param("isDeleted", "false")
                        .param("sortField", "id")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service categories retrieved successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].categoryName").value("Hotel"));
    }
}
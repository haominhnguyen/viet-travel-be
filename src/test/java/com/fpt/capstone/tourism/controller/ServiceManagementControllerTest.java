package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ChangeStatusRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceResponseDTO;
import com.fpt.capstone.tourism.service.ServiceCategoryService;
import com.fpt.capstone.tourism.service.ServiceService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceService serviceService;

    @Mock
    private ServiceCategoryService serviceCategoryService;

    @InjectMocks
    private ServiceManagementController serviceManagementController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(serviceManagementController).build();

        // Configure ObjectMapper for handling LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetAllServiceCategories() throws Exception {
        // Arrange
        List<ServiceCategoryDTO> categories = new ArrayList<>();
        categories.add(ServiceCategoryDTO.builder()
                .id(1L)
                .categoryName("Hotel")
                .deleted(false)
                .build());
        categories.add(ServiceCategoryDTO.builder()
                .id(2L)
                .categoryName("Restaurant")
                .deleted(false)
                .build());

        GeneralResponse<List<ServiceCategoryDTO>> response =
                new GeneralResponse<>(200, "Categories loaded successfully", categories);

        when(serviceCategoryService.getAllServiceCategories()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/ceo/services/list-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Categories loaded successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].categoryName").value("Restaurant"));
    }

    @Test
    void testGetServices() throws Exception {
        // Arrange
        List<ServiceBaseDTO> services = new ArrayList<>();
        services.add(ServiceBaseDTO.builder()
                .id(1L)
                .name("Luxury Hotel")
                .nettPrice(100.0)
                .sellingPrice(150.0)
                .imageUrl("http://example.com/hotel.jpg")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .deleted(false)
                .serviceCategoryId(1L)
                .serviceCategoryName("Hotel")
                .serviceProviderId(1L)
                .serviceProviderName("Provider 1")
                .serviceProviderAbbreviation("P1")
                .serviceProviderImageUrl("http://example.com/provider.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        PagingDTO<List<ServiceBaseDTO>> pagingDTO = PagingDTO.<List<ServiceBaseDTO>>builder()
                .page(0)
                .size(10)
                .total(1)
                .items(services)
                .build();

        GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> response =
                new GeneralResponse<>(200, "Services loaded successfully", pagingDTO);

        when(serviceService.getAllServices(
                anyInt(), anyInt(), anyString(), anyBoolean(),
                anyString(), anyString(), anyLong()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/ceo/services/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "hotel")
                        .param("isDeleted", "false")
                        .param("providerId", "1")
                        .param("sortField", "id")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Services loaded successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Luxury Hotel"))
                .andExpect(jsonPath("$.data.items[0].serviceCategoryName").value("Hotel"));
    }

    @Test
    void testGetTourDayServicesByService() throws Exception {
        // Arrange
        List<TourDayServiceDTO> tourDayServices = new ArrayList<>();
        TourDayDTO tourDay = new TourDayDTO();
        tourDay.setId(1L);
        tourDay.setDayNumber(1);

        tourDayServices.add(TourDayServiceDTO.builder()
                .id(1L)
                .quantity(2)
                .sellingPrice(300.0)
                .tourDay(tourDay)
                .build());

        GeneralResponse<List<TourDayServiceDTO>> response =
                new GeneralResponse<>(200, "Tour day services loaded successfully", tourDayServices);

        when(serviceService.getTourDayServicesByServiceId(eq(1L), isNull())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/ceo/services/tour-day-services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour day services loaded successfully"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].quantity").value(2))
                .andExpect(jsonPath("$.data[0].sellingPrice").value(300.0))
                .andExpect(jsonPath("$.data[0].tourDay.id").value(1))
                .andExpect(jsonPath("$.data[0].tourDay.dayNumber").value(1));
    }

    @Test
    void testGetServiceDetailsByService() throws Exception {
        // Arrange
        Map<String, Object> serviceDetails = new HashMap<>();
        serviceDetails.put("id", 1L);
        serviceDetails.put("name", "Luxury Hotel");
        serviceDetails.put("nettPrice", 100.0);
        serviceDetails.put("sellingPrice", 150.0);
        serviceDetails.put("imageUrl", "http://example.com/hotel.jpg");
        serviceDetails.put("categoryId", 1L);
        serviceDetails.put("categoryName", "Hotel");
        serviceDetails.put("providerId", 1L);
        serviceDetails.put("providerName", "Provider 1");

        RoomDTO roomDetails = RoomDTO.builder()
                .id(1L)
                .capacity(2)
                .availableQuantity(10)
                .deleted(false)
                .serviceId(1L)
                .facilities("WiFi, TV, Mini Bar")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .categoryId(1L)
                .categoryName("Hotel")
                .build();

        serviceDetails.put("roomDetails", roomDetails);

        GeneralResponse<Object> response =
                new GeneralResponse<>(200, "Service details loaded successfully", serviceDetails);

        when(serviceService.getServiceDetailsByServiceId(eq(1L), isNull())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/ceo/services/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service details loaded successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Luxury Hotel"))
                .andExpect(jsonPath("$.data.categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data.roomDetails.capacity").value(2))
                .andExpect(jsonPath("$.data.roomDetails.availableQuantity").value(10))
                .andExpect(jsonPath("$.data.roomDetails.facilities").value("WiFi, TV, Mini Bar"));
    }

    @Test
    void testCreateService() throws Exception {
        // Arrange
        ServiceRequestDTO requestDTO = new ServiceRequestDTO();
        requestDTO.setName("New Luxury Hotel");
        requestDTO.setNettPrice(100.0);
        requestDTO.setSellingPrice(150.0);
        requestDTO.setCategoryId(1L);
        requestDTO.setProviderId(1L);

        ServiceResponseDTO responseDTO = new ServiceResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("New Luxury Hotel");
        responseDTO.setNettPrice(100.0);
        responseDTO.setSellingPrice(150.0);
        responseDTO.setCategoryId(1L);
        responseDTO.setCategoryName("Hotel");
        responseDTO.setProviderId(1L);
        responseDTO.setProviderName("Provider 1");
        responseDTO.setCreatedAt(LocalDateTime.now());

        RoomDTO roomDetails = new RoomDTO();
        roomDetails.setCapacity(2);
        roomDetails.setAvailableQuantity(10);
        roomDetails.setFacilities("WiFi,TV,Mini Bar");
        responseDTO.setRoomDetails(roomDetails);

        GeneralResponse<ServiceResponseDTO> response =
                new GeneralResponse<>(201, "Service created successfully", responseDTO);

        when(serviceService.createService(any(ServiceRequestDTO.class), eq(1L))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/ceo/services/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Service created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("New Luxury Hotel"))
                .andExpect(jsonPath("$.data.categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data.providerName").value("Provider 1"))
                .andExpect(jsonPath("$.data.roomDetails.capacity").value(2));
    }

    @Test
    void testUpdateService() throws Exception {
        // Arrange
        ServiceRequestDTO requestDTO = new ServiceRequestDTO();
        requestDTO.setName("Updated Luxury Hotel");
        requestDTO.setNettPrice(120.0);
        requestDTO.setSellingPrice(180.0);
        requestDTO.setCategoryId(1L);
        requestDTO.setProviderId(1L);

        ServiceResponseDTO responseDTO = new ServiceResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Updated Luxury Hotel");
        responseDTO.setNettPrice(120.0);
        responseDTO.setSellingPrice(180.0);
        responseDTO.setCategoryId(1L);
        responseDTO.setCategoryName("Hotel");
        responseDTO.setProviderId(1L);
        responseDTO.setProviderName("Provider 1");
        responseDTO.setUpdatedAt(LocalDateTime.now());

        RoomDTO roomDetails = new RoomDTO();
        roomDetails.setCapacity(2);
        roomDetails.setAvailableQuantity(10);
        roomDetails.setFacilities("WiFi,TV,Mini Bar");
        responseDTO.setRoomDetails(roomDetails);

        GeneralResponse<ServiceResponseDTO> response =
                new GeneralResponse<>(200, "Service updated successfully", responseDTO);

        when(serviceService.updateService(eq(1L), any(ServiceRequestDTO.class), eq(1L))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/ceo/services/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Updated Luxury Hotel"))
                .andExpect(jsonPath("$.data.nettPrice").value(120.0))
                .andExpect(jsonPath("$.data.sellingPrice").value(180.0));
    }

    @Test
    void testChangeServiceStatus() throws Exception {
        // Arrange
        ChangeStatusRequestDTO requestDTO = new ChangeStatusRequestDTO();
        requestDTO.setIsDeleted(true);
        requestDTO.setProviderId(1L);

        ServiceResponseDTO responseDTO = new ServiceResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Luxury Hotel");
        responseDTO.setDeleted(true);
        responseDTO.setUpdatedAt(LocalDateTime.now());

        GeneralResponse<ServiceResponseDTO> response =
                new GeneralResponse<>(200, "Service status changed successfully", responseDTO);

        when(serviceService.changeServiceStatus(eq(1L), eq(true), eq(1L))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/ceo/services/change-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service status changed successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }
}
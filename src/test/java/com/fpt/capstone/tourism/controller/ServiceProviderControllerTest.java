package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceProviderDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.ServiceProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceProviderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceProviderService serviceProviderService;

    @InjectMocks
    private ServiceProviderController serviceProviderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(serviceProviderController).build();
    }

    @Test
    void testCreateServiceProvider() throws Exception {
        ServiceProviderDTO requestDTO = new ServiceProviderDTO();
        requestDTO.setName("Test Provider");
        requestDTO.setEmail("test@provider.com");

        ServiceProviderDTO responseDTO = new ServiceProviderDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Provider");
        responseDTO.setEmail("test@provider.com");

        when(serviceProviderService.save(any(ServiceProviderDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Provider created successfully", responseDTO));

        mockMvc.perform(post("/ceo/service-provider/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Provider"))
                .andExpect(jsonPath("$.data.email").value("test@provider.com"));
    }

    @Test
    void testGetServiceProviderDetails() throws Exception {
        ServiceProviderDTO responseDTO = new ServiceProviderDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Provider");
        responseDTO.setEmail("test@provider.com");

        when(serviceProviderService.getServiceProviderById(1L))
                .thenReturn(new GeneralResponse<>(200, "Success", responseDTO));

        mockMvc.perform(get("/ceo/service-provider/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Provider"));
    }

    @Test
    void testGetAllServiceProviders() throws Exception {
        ServiceProviderDTO responseDTO = new ServiceProviderDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Provider");

        List<ServiceProviderDTO> providers = Collections.singletonList(responseDTO);
        PagingDTO<List<ServiceProviderDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, providers);

        when(serviceProviderService.getAllServiceProviders(0, 10, null, null))
                .thenReturn(new GeneralResponse<>(200, "Success", pagingDTO));

        mockMvc.perform(get("/ceo/service-provider/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("Test Provider"));
    }

    @Test
    void testUpdateServiceProvider() throws Exception {
        ServiceProviderDTO requestDTO = new ServiceProviderDTO();
        requestDTO.setName("Updated Provider");

        when(serviceProviderService.updateServiceProvider(eq(1L), any(ServiceProviderDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Provider updated successfully", null));

        mockMvc.perform(put("/ceo/service-provider/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Provider updated successfully"));
    }

    @Test
    void testDeleteServiceProvider() throws Exception {
        when(serviceProviderService.deleteServiceProvider(1L, true))
                .thenReturn(new GeneralResponse<>(200, "Provider status updated", null));

        mockMvc.perform(delete("/ceo/service-provider/change-status/1")
                        .param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Provider status updated"));
    }
}

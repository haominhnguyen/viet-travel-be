package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.LocationService;
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

import com.fasterxml.jackson.databind.ObjectMapper;

class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    @Test
    void testCreateLocation() throws Exception {
        LocationRequestDTO locationRequestDTO = new LocationRequestDTO();
        locationRequestDTO.setName("Da Nang");

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setName("Da Nang");

        when(locationService.saveLocation(any(LocationRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Location created successfully", locationDTO));

        mockMvc.perform(post("/head-business/location/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Location created successfully"))
                .andExpect(jsonPath("$.data.name").value("Da Nang"));
    }

    @Test
    void testGetLocationDetails() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setName("Da Nang");

        when(locationService.getLocationById(1L))
                .thenReturn(new GeneralResponse<>(200, "Success", locationDTO));

        mockMvc.perform(get("/head-business/location/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Da Nang"));
    }

    @Test
    void testGetAllLocations() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setName("Ha Long");

        List<LocationDTO> locationList = Collections.singletonList(locationDTO);
        PagingDTO<List<LocationDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, locationList);

        when(locationService.getAllLocation(0, 10, null, null, "desc"))
                .thenReturn(new GeneralResponse<>(200, "Success", pagingDTO));

        mockMvc.perform(get("/head-business/location/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderDate", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("Ha Long"));
    }

    @Test
    void testUpdateLocation() throws Exception {
        LocationRequestDTO locationRequestDTO = new LocationRequestDTO();
        locationRequestDTO.setName("Updated Da Nang");

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setName("Updated Da Nang");

        when(locationService.updateLocation(eq(1L), any(LocationRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Location updated successfully", locationDTO));

        mockMvc.perform(put("/head-business/location/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Location updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Da Nang"));
    }

    @Test
    void testChangeLocationStatus() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setDeleted(true);

        when(locationService.deleteLocation(1L, true))
                .thenReturn(new GeneralResponse<>(200, "Location status changed successfully", locationDTO));

        mockMvc.perform(delete("/head-business/location/change-status/1")
                        .param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Location status changed successfully"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }
}

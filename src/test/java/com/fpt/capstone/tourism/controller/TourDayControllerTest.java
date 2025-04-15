package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateRequestDTO;
import com.fpt.capstone.tourism.service.TourDayServiceI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TourDayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TourDayServiceI tourDayService;

    @InjectMocks
    private TourDayController tourDayController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private TourDayFullDTO tourDayFullDTO;
    private TourDayCreateRequestDTO createRequestDTO;
    private TourDayUpdateRequestDTO updateRequestDTO;
    private List<TourDayFullDTO> tourDayList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tourDayController).build();

        // Configure ObjectMapper for LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Initialize test data
        LocationDTO locationDTO = LocationDTO.builder()
                .id(1L)
                .name("Test Location")
                .build();

        tourDayFullDTO = TourDayFullDTO.builder()
                .id(1L)
                .title("Day 1 - Welcome")
                .dayNumber(1)
                .content("Welcome and introduction")
                .mealPlan("Breakfast, Lunch, Dinner")
                .tourId(1L)
                .location(locationDTO)
                .deleted(false)
                .serviceCategories(Arrays.asList("Hotel", "Transport"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tourDayList = Collections.singletonList(tourDayFullDTO);

        createRequestDTO = TourDayCreateRequestDTO.builder()
                .title("Day 2 - Exploration")
                .content("City tour and exploration")
                .mealPlan("Breakfast, Lunch")
                .locationId(1L)
                .serviceCategories(Arrays.asList("Hotel", "Transport"))
                .build();

        updateRequestDTO = TourDayUpdateRequestDTO.builder()
                .dayNumber(1)
                .title("Day 1 - Updated")
                .content("Updated content")
                .mealPlan("Breakfast, Dinner")
                .locationId(1L)
                .serviceCategories(Arrays.asList("Hotel", "Transport", "Activity"))
                .build();
    }

    @Test
    void testGetTourDaysByTourId() throws Exception {
        // Arrange
        when(tourDayService.getTourDayDetail(eq(1L), isNull()))
                .thenReturn(new GeneralResponse<>(200, "Tour days retrieved successfully", tourDayList));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/tour-days/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour days retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Day 1 - Welcome"))
                .andExpect(jsonPath("$.data[0].dayNumber").value(1))
                .andExpect(jsonPath("$.data[0].tourId").value(1))
                .andExpect(jsonPath("$.data[0].deleted").value(false))
                .andExpect(jsonPath("$.data[0].serviceCategories[0]").value("Hotel"));
    }

    @Test
    void testGetTourDaysByTourIdWithDeletedParam() throws Exception {
        // Arrange
        when(tourDayService.getTourDayDetail(eq(1L), eq(false)))
                .thenReturn(new GeneralResponse<>(200, "Active tour days retrieved successfully", tourDayList));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/tour-days/list")
                        .param("isDeleted", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Active tour days retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void testCreateTourDay() throws Exception {
        // Arrange
        when(tourDayService.createTourDay(eq(1L), any(TourDayCreateRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Tour day created successfully", tourDayFullDTO));

        // Act & Assert
        mockMvc.perform(post("/head-of-business/tour/1/tour-days/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour day created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Day 1 - Welcome"))
                .andExpect(jsonPath("$.data.tourId").value(1));
    }

    @Test
    void testUpdateTourDay() throws Exception {
        // Arrange
        TourDayFullDTO updatedTourDay = TourDayFullDTO.builder()
                .id(1L)
                .title("Day 1 - Updated")
                .dayNumber(1)
                .content("Updated content")
                .mealPlan("Breakfast, Dinner")
                .tourId(1L)
                .deleted(false)
                .serviceCategories(Arrays.asList("Hotel", "Transport", "Activity"))
                .build();

        when(tourDayService.updateTourDay(eq(1L), eq(1L), any(TourDayUpdateRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Tour day updated successfully", updatedTourDay));

        // Act & Assert
        mockMvc.perform(put("/head-of-business/tour/1/tour-days/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour day updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Day 1 - Updated"))
                .andExpect(jsonPath("$.data.content").value("Updated content"))
                .andExpect(jsonPath("$.data.serviceCategories.length()").value(3));
    }

    @Test
    void testChangeTourDayStatus() throws Exception {
        // Arrange
        when(tourDayService.changeTourDayStatus(eq(1L), eq(1L), eq(true)))
                .thenReturn(new GeneralResponse<>(200, "Tour day status changed successfully", "Tour day has been deleted"));

        // Act & Assert
        mockMvc.perform(put("/head-of-business/tour/1/tour-days/1/status")
                        .param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour day status changed successfully"))
                .andExpect(jsonPath("$.data").value("Tour day has been deleted"));
    }
}
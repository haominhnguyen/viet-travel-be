package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourPaxFullDTO;
import com.fpt.capstone.tourism.dto.request.TourMarkupUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourMarkupResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourResponseDTO;
import com.fpt.capstone.tourism.service.TourPaxService;
import com.fpt.capstone.tourism.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TourPaxControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TourPaxService tourPaxService;

    @Mock
    private TourService tourService;

    @InjectMocks
    private TourPaxController tourPaxController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TourPaxFullDTO mockTourPaxFullDTO;
    private TourPaxCreateRequestDTO mockCreateRequestDTO;
    private TourPaxUpdateRequestDTO mockUpdateRequestDTO;
    private TourMarkupResponseDTO mockMarkupResponseDTO;
    private TourResponseDTO mockTourResponseDTO;
    private List<TourPaxFullDTO> mockTourPaxList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tourPaxController).build();

        // Configure ObjectMapper for handling dates
        objectMapper.registerModule(new JavaTimeModule());

        // Set up test data
        setupTestData();
    }

    private void setupTestData() {
        // Create a TourPaxFullDTO
        mockTourPaxFullDTO = TourPaxFullDTO.builder()
                .id(1L)
                .tourId(1L)
                .minPax(2)
                .maxPax(10)
                .paxRange("2-10")
                .fixedCost(500.0)
                .extraHotelCost(100.0)
                .nettPricePerPax(800.0)
                .sellingPrice(1000.0)
                .validFrom(new Date())
                .validTo(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 days in the future
                .isValid(true)
                .isDeleted(false)
                .serviceAssociations(new ArrayList<>())
                .serviceAssociationCount(0)
                .build();

        // Create list of TourPaxFullDTO
        mockTourPaxList = new ArrayList<>();
        mockTourPaxList.add(mockTourPaxFullDTO);

        // Create TourPaxCreateRequestDTO
        mockCreateRequestDTO = TourPaxCreateRequestDTO.builder()
                .minPax(2)
                .maxPax(10)
                .fixedCost(500.0)
                .extraHotelCost(100.0)
                .nettPricePerPax(800.0)
                .sellingPrice(1000.0)
                .validFrom(new Date())
                .validTo(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 days in the future
                .servicePricings(new ArrayList<>())
                .build();

        // Create TourPaxUpdateRequestDTO
        mockUpdateRequestDTO = TourPaxUpdateRequestDTO.builder()
                .minPax(3)
                .maxPax(12)
                .fixedCost(550.0)
                .extraHotelCost(120.0)
                .nettPricePerPax(850.0)
                .sellingPrice(1100.0)
                .validFrom(new Date())
                .validTo(new Date(System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000)) // 45 days in the future
                .servicePricings(new ArrayList<>())
                .build();

        // Create TourMarkupResponseDTO
        mockMarkupResponseDTO = TourMarkupResponseDTO.builder()
                .tourId(1L)
                .tourName("Amazing Vietnam Tour")
                .markUpPercent(20.0)
                .build();

        // Create TourResponseDTO
        mockTourResponseDTO = TourResponseDTO.builder()
                .id(1L)
                .name("Amazing Vietnam Tour")
                .markUpPercent(25.0) // Updated markup
                .build();
    }

    @Test
    void testGetTourPaxConfigurations() throws Exception {
        // Arrange
        GeneralResponse<List<TourPaxFullDTO>> response = new GeneralResponse<>(200, "Success", mockTourPaxList);
        when(tourPaxService.getTourPaxConfigurations(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/tour-pax"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].tourId").value(1L))
                .andExpect(jsonPath("$.data[0].minPax").value(2))
                .andExpect(jsonPath("$.data[0].maxPax").value(10));

        // Verify
        verify(tourPaxService).getTourPaxConfigurations(1L);
    }

    @Test
    void testGetTourPaxConfiguration() throws Exception {
        // Arrange
        GeneralResponse<TourPaxFullDTO> response = new GeneralResponse<>(200, "Success", mockTourPaxFullDTO);
        when(tourPaxService.getTourPaxConfiguration(1L, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/tour-pax/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.tourId").value(1L))
                .andExpect(jsonPath("$.data.minPax").value(2))
                .andExpect(jsonPath("$.data.maxPax").value(10));

        // Verify
        verify(tourPaxService).getTourPaxConfiguration(1L, 1L);
    }

    @Test
    void testCreateTourPaxConfiguration() throws Exception {
        // Arrange
        GeneralResponse<TourPaxFullDTO> response = new GeneralResponse<>(201, "Tour pax configuration created successfully", mockTourPaxFullDTO);
        when(tourPaxService.createTourPaxConfiguration(eq(1L), any(TourPaxCreateRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/head-of-business/tour/1/tour-pax/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCreateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Tour pax configuration created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.tourId").value(1L))
                .andExpect(jsonPath("$.data.minPax").value(2))
                .andExpect(jsonPath("$.data.maxPax").value(10));

        // Verify
        verify(tourPaxService).createTourPaxConfiguration(eq(1L), any(TourPaxCreateRequestDTO.class));
    }

//    @Test
//    void testUpdateTourPaxConfiguration() throws Exception {
//        // Arrange
//        TourPaxFullDTO updatedDTO = mockTourPaxFullDTO.builder()
//                .minPax(3)
//                .maxPax(12)
//                .fixedCost(550.0)
//                .build();
//
//        GeneralResponse<TourPaxFullDTO> response = new GeneralResponse<>(200, "Tour pax configuration updated successfully", updatedDTO);
//        when(tourPaxService.updateTourPaxConfiguration(eq(1L), eq(1L), any(TourPaxUpdateRequestDTO.class))).thenReturn(response);
//
//        // Act & Assert
//        mockMvc.perform(put("/head-of-business/tour/1/tour-pax/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(mockUpdateRequestDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("Tour pax configuration updated successfully"))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.tourId").value(1L))
//                .andExpect(jsonPath("$.data.minPax").value(3))
//                .andExpect(jsonPath("$.data.maxPax").value(12));
//
//        // Verify
//        verify(tourPaxService).updateTourPaxConfiguration(eq(1L), eq(1L), any(TourPaxUpdateRequestDTO.class));
//    }

    @Test
    void testDeleteTourPaxConfiguration() throws Exception {
        // Arrange
        GeneralResponse<String> response = new GeneralResponse<>(200, "Tour pax configuration deleted successfully", "Deleted");
        when(tourPaxService.deleteTourPaxConfiguration(1L, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/head-of-business/tour/1/tour-pax/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour pax configuration deleted successfully"))
                .andExpect(jsonPath("$.data").value("Deleted"));

        // Verify
        verify(tourPaxService).deleteTourPaxConfiguration(1L, 1L);
    }

    @Test
    void testGetTourMarkupPercentage() throws Exception {
        // Arrange
        GeneralResponse<TourMarkupResponseDTO> response = new GeneralResponse<>(200, "Success", mockMarkupResponseDTO);
        when(tourService.getTourMarkupPercentage(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/tour-pax/markup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.tourId").value(1L))
                .andExpect(jsonPath("$.data.tourName").value("Amazing Vietnam Tour"))
                .andExpect(jsonPath("$.data.markUpPercent").value(20.0));

        // Verify
        verify(tourService).getTourMarkupPercentage(1L);
    }

    @Test
    void testUpdateTourMarkupPercentage() throws Exception {
        // Arrange
        TourMarkupUpdateRequestDTO requestDTO = new TourMarkupUpdateRequestDTO();
        requestDTO.setMarkUpPercent(25.0);

        GeneralResponse<TourResponseDTO> response = new GeneralResponse<>(200, "Markup percentage updated successfully", mockTourResponseDTO);
        when(tourService.updateTourMarkupPercentage(eq(1L), eq(25.0))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/head-of-business/tour/1/tour-pax/update-markup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Markup percentage updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Amazing Vietnam Tour"))
                .andExpect(jsonPath("$.data.markUpPercent").value(25.0));

        // Verify
        verify(tourService).updateTourMarkupPercentage(1L, 25.0);
    }
}
package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourPriceConfigRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceConfigResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceListResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.TourPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TourPriceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TourPriceService tourPriceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private TourPriceController tourPriceController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TourPriceConfigRequestDTO requestDTO;
    private TourPriceConfigResponseDTO responseDTO;
    private TourPriceListResponseDTO listResponseDTO;
    private User mockUser;
    private final Long TOUR_ID = 1L;
    private final Long CONFIG_ID = 1L;
    private final Date currentDate = new Date();
    private final Date futureDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // 30 days in the future

    @BeforeEach
    void setUp() {
        // Configure mockMvc with custom argument resolver for @AuthenticationPrincipal
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails; // Return the mocked UserDetails
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(tourPriceController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();

        // Configure ObjectMapper for handling dates and times
        objectMapper.registerModule(new JavaTimeModule());

        // Initialize test data
        setupTestData();
    }

    private void setupTestData() {
        // Create mock User
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setFullName("Test User");
        mockUser.setEmail("test@example.com");

        // Create request DTO
        requestDTO = TourPriceConfigRequestDTO.builder()
                .id(CONFIG_ID)
                .tourId(TOUR_ID)
                .sellingPrice(1000.0)
                .fixedCost(500.0)
                .extraHotelCost(100.0)
                .nettPricePerPax(300.0)
                .validFrom(currentDate)
                .validTo(futureDate)
                .build();

        // Create response DTO
        responseDTO = TourPriceConfigResponseDTO.builder()
                .id(CONFIG_ID)
                .tourId(TOUR_ID)
                .tourName("Amazing Vietnam Tour")
                .minPax(2)
                .maxPax(10)
                .paxRange("2-10")
                .nettPricePerPax(300.0)
                .sellingPrice(1000.0)
                .fixedCost(500.0)
                .extraHotelCost(100.0)
                .validFrom(currentDate)
                .validTo(futureDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create list response DTO
        List<TourPriceConfigResponseDTO> priceConfigurations = new ArrayList<>();
        priceConfigurations.add(responseDTO);

        listResponseDTO = TourPriceListResponseDTO.builder()
                .tourId(TOUR_ID)
                .tourName("Amazing Vietnam Tour")
                .priceConfigurations(priceConfigurations)
                .build();
    }

    @Test
    void configureTourPrice_Success() throws Exception {
        // Arrange
        GeneralResponse<TourPriceConfigResponseDTO> response =
                new GeneralResponse<>(HttpStatus.OK.value(), "Price configuration updated successfully", responseDTO);

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(tourPriceService.updateTourPrice(any(TourPriceConfigRequestDTO.class), eq(mockUser))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/head-of-business/tour/{tourId}/price-configurations/manage", TOUR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Price configuration updated successfully"))
                .andExpect(jsonPath("$.data.id").value(CONFIG_ID))
                .andExpect(jsonPath("$.data.tourId").value(TOUR_ID))
                .andExpect(jsonPath("$.data.tourName").value("Amazing Vietnam Tour"))
                .andExpect(jsonPath("$.data.minPax").value(2))
                .andExpect(jsonPath("$.data.maxPax").value(10))
                .andExpect(jsonPath("$.data.paxRange").value("2-10"))
                .andExpect(jsonPath("$.data.nettPricePerPax").value(300.0))
                .andExpect(jsonPath("$.data.sellingPrice").value(1000.0))
                .andExpect(jsonPath("$.data.fixedCost").value(500.0))
                .andExpect(jsonPath("$.data.extraHotelCost").value(100.0));
    }

//    @Test
//    void configureTourPrice_UserNotFound() throws Exception {
//        // Arrange
//        when(userDetails.getUsername()).thenReturn("nonexistentuser");
//        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        mockMvc.perform(post("/head-of-business/tour/{tourId}/price-configurations/manage", TOUR_ID)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void configureTourPrice_NullUser() throws Exception {
//        // Arrange - setting userDetails to null will trigger the authentication check
//        // Need to create a new resolver that returns null
//        HandlerMethodArgumentResolver nullAuthResolver = new HandlerMethodArgumentResolver() {
//            @Override
//            public boolean supportsParameter(MethodParameter parameter) {
//                return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
//            }
//
//            @Override
//            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
//                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
//                return null; // Return null UserDetails
//            }
//        };
//
//        // Rebuild mockMvc with null resolver
//        MockMvc mockMvcWithNullUser = MockMvcBuilders.standaloneSetup(tourPriceController)
//                .setCustomArgumentResolvers(nullAuthResolver)
//                .build();
//
//        // Act & Assert
//        mockMvcWithNullUser.perform(post("/head-of-business/tour/{tourId}/price-configurations/manage", TOUR_ID)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isUnauthorized());
//    }
//
    @Test
    void getTourPriceConfigurations_Success() throws Exception {
        // Arrange
        GeneralResponse<TourPriceListResponseDTO> response =
                new GeneralResponse<>(HttpStatus.OK.value(), "Price configurations retrieved successfully", listResponseDTO);

        when(tourPriceService.getTourPriceConfigurations(TOUR_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/{tourId}/price-configurations/list", TOUR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Price configurations retrieved successfully"))
                .andExpect(jsonPath("$.data.tourId").value(TOUR_ID))
                .andExpect(jsonPath("$.data.tourName").value("Amazing Vietnam Tour"))
                .andExpect(jsonPath("$.data.priceConfigurations[0].id").value(CONFIG_ID))
                .andExpect(jsonPath("$.data.priceConfigurations[0].tourId").value(TOUR_ID))
                .andExpect(jsonPath("$.data.priceConfigurations[0].minPax").value(2))
                .andExpect(jsonPath("$.data.priceConfigurations[0].maxPax").value(10));
    }

    @Test
    void getTourPriceConfigurationById_Success() throws Exception {
        // Arrange
        GeneralResponse<TourPriceConfigResponseDTO> response =
                new GeneralResponse<>(HttpStatus.OK.value(), "Price configuration retrieved successfully", responseDTO);

        when(tourPriceService.getTourPriceConfigurationById(TOUR_ID, CONFIG_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/{tourId}/price-configurations/details/{configId}", TOUR_ID, CONFIG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Price configuration retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(CONFIG_ID))
                .andExpect(jsonPath("$.data.tourId").value(TOUR_ID))
                .andExpect(jsonPath("$.data.tourName").value("Amazing Vietnam Tour"))
                .andExpect(jsonPath("$.data.minPax").value(2))
                .andExpect(jsonPath("$.data.maxPax").value(10))
                .andExpect(jsonPath("$.data.paxRange").value("2-10"))
                .andExpect(jsonPath("$.data.nettPricePerPax").value(300.0))
                .andExpect(jsonPath("$.data.sellingPrice").value(1000.0))
                .andExpect(jsonPath("$.data.fixedCost").value(500.0))
                .andExpect(jsonPath("$.data.extraHotelCost").value(100.0));
    }

//    @Test
//    void getTourPriceConfigurationById_NotFound() throws Exception {
//        // Arrange
//        when(tourPriceService.getTourPriceConfigurationById(TOUR_ID, 999L))
//                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND.value(), "Price configuration not found", null));
//
//        // Act & Assert
//        mockMvc.perform(get("/head-of-business/tour/{tourId}/price-configurations/details/{configId}", TOUR_ID, 999L))
//                .andExpect(status().isNotFound());
//    }

//    @Test
//    void getTourPriceConfigurations_TourNotFound() throws Exception {
//        // Arrange
//        when(tourPriceService.getTourPriceConfigurations(999L))
//                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour not found", null));
//
//        // Act & Assert
//        mockMvc.perform(get("/head-of-business/tour/{tourId}/price-configurations/list", 999L))
//                .andExpect(status().isNotFound());
//    }

}
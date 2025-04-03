package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourGuideResponseDTO;
import com.fpt.capstone.tourism.enums.Gender;
import com.fpt.capstone.tourism.service.TourGuideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TourGuideControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TourGuideService tourGuideService;

    @InjectMocks
    private TourGuideController tourGuideController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tourGuideController).build();
    }

    @Test
    void testCreateTourGuide() throws Exception {
        TourGuideRequestDTO requestDTO = TourGuideRequestDTO.builder()
                .fullName("John Doe")
                .username("johndoe")
                .password("SecurePass123!")
                .email("johndoe@example.com")
                .gender(Gender.MALE)
                .phone("1234567890")
                .address("123 Street, City")
                .avatarImage("avatar_url")
                .build();

        when(tourGuideService.create(any(TourGuideRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Tour guide created successfully", null));

        mockMvc.perform(post("/head-of-business/tour-guides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tour guide created successfully"));
    }

    @Test
    void testGetAllTourGuides() throws Exception {
        TourGuideResponseDTO responseDTO = new TourGuideResponseDTO();
        responseDTO.setFullName("John Doe");

        List<TourGuideResponseDTO> guideList = Arrays.asList(responseDTO);
        PagingDTO<List<TourGuideResponseDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, guideList);

        when(tourGuideService.getAll(0, 10, null, null, null, "createdAt", "desc"))
                .thenReturn(new GeneralResponse<>(200, "Success", pagingDTO));

        mockMvc.perform(get("/head-of-business/tour-guides")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortField", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].fullName").value("John Doe"));
    }

    @Test
    void testGetTourGuideById() throws Exception {
        TourGuideResponseDTO responseDTO = new TourGuideResponseDTO();
        responseDTO.setFullName("John Doe");

        when(tourGuideService.getById(1L))
                .thenReturn((GeneralResponse) new GeneralResponse<>(200, "Success", responseDTO));


        mockMvc.perform(get("/head-of-business/tour-guides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    void testUpdateTourGuide() throws Exception {
        TourGuideRequestDTO requestDTO = new TourGuideRequestDTO();
        requestDTO.setFullName("Updated Name");

        when(tourGuideService.update(eq(1L), any(TourGuideRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Tour guide updated successfully", null));

        mockMvc.perform(put("/head-of-business/tour-guides/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tour guide updated successfully"));
    }

    @Test
    void testChangeTourGuideStatus() throws Exception {
        when(tourGuideService.delete(1L, true))
                .thenReturn(new GeneralResponse<>(200, "Tour guide status updated", null));

        mockMvc.perform(post("/head-of-business/tour-guides/change-status/1")
                        .param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tour guide status updated"));
    }
}

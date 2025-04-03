package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;

class ActivityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ActivityController activityController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(activityController).build();
    }

    @Test
    void testCreateActivity() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTitle("Hiking in the Mountains");

        when(activityService.saveActivity(any(ActivityDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Created successfully", activityDTO));

        mockMvc.perform(post("/head-business/activity/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Created successfully"))
                .andExpect(jsonPath("$.data.title").value("Hiking in the Mountains"));
    }

    @Test
    void testGetActivityDetails() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setTitle("Hiking Adventure");

        when(activityService.getActivityById(1L))
                .thenReturn(new GeneralResponse<>(200, "Success", activityDTO));

        mockMvc.perform(get("/head-business/activity/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Hiking Adventure"));
    }

    @Test
    void testGetAllActivities() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setTitle("Kayaking");

        List<ActivityDTO> activityList = Collections.singletonList(activityDTO);
        PagingDTO<List<ActivityDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, activityList);

        when(activityService.getAllActivity(0, 10, null, null, "desc", null))
                .thenReturn(new GeneralResponse<>(200, "Success", pagingDTO));

        mockMvc.perform(get("/head-business/activity/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderDate", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].title").value("Kayaking"));
    }

    @Test
    void testUpdateActivity() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setTitle("Updated Hiking");

        when(activityService.updateActivity(eq(1L), any(ActivityDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Updated successfully", activityDTO));

        mockMvc.perform(put("/head-business/activity/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Hiking"));
    }

    @Test
    void testDeleteActivity() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setDeleted(true);

        when(activityService.deleteActivity(1L, true))
                .thenReturn(new GeneralResponse<>(200, "Deleted successfully", activityDTO));

        mockMvc.perform(delete("/head-business/activity/change-status/1")
                        .param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }
}

package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ActivityMapper;
import com.fpt.capstone.tourism.model.Activity;
import com.fpt.capstone.tourism.repository.ActivityRepository;
import com.fpt.capstone.tourism.service.*;
import com.fpt.capstone.tourism.service.impl.HomepageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomepageServiceImplTest {

    @InjectMocks
    private HomepageServiceImpl homepageService;

    @Mock
    private TourService tourService;
    @Mock
    private BlogService blogService;
    @Mock
    private ActivityService activityService;
    @Mock
    private ServiceProviderService providerService;
    @Mock
    private LocationService locationService;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private ActivityMapper activityMapper;

    private TourDTO mockTour;
    private BlogResponseDTO mockBlog;
    private ActivityDTO mockActivity;
    private LocationDTO mockLocation;

    @BeforeEach
    void setUp() {
        mockTour = new TourDTO();
        mockBlog = new BlogResponseDTO();
        mockActivity = new ActivityDTO();
        mockLocation = new LocationDTO();
    }

    @Test
    void viewHomepage_Success() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(5)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(5)).thenReturn(Collections.singletonList(mockBlog));
        when(activityService.findRecommendedActivities(5)).thenReturn(Collections.singletonList(mockActivity));
        when(locationService.findRecommendedLocations(5)).thenReturn(Collections.singletonList(mockLocation));

        GeneralResponse<HomepageDTO> response = homepageService.viewHomepage(5, 5, 5, 5);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Homepage loaded successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getNewBlogs().size());
        assertEquals(1, response.getData().getTrendingTours().size());
    }

    @Test
    void viewHomepage_Failure() {
        when(tourService.findTopTourOfYear()).thenThrow(new RuntimeException("Database error"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                homepageService.viewHomepage(5, 5, 5, 5));

        assertEquals("Error retrieve homepage data", exception.getMessage());
    }

    @Test
    void viewAllHotel_Success() {
        PagingDTO<List<ServiceProviderDTO>> pagingDTO = new PagingDTO<>();
        GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> expectedResponse = new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);

        when(providerService.getAllHotel(1, 10, "hotel")).thenReturn(expectedResponse);

        GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> response = homepageService.viewAllHotel(1, 10, "hotel");

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Success", response.getMessage());
    }

    @Test
    void viewPublicActivityDetail_Success() {
        Activity mockEntity = new Activity();
        when(activityRepository.findById(1L)).thenReturn(java.util.Optional.of(mockEntity));
        when(activityMapper.toDTO(mockEntity)).thenReturn(mockActivity);
        when(activityService.findRelatedActivities(1L, 5)).thenReturn(Collections.singletonList(mockActivity));

        GeneralResponse<PublicActivityDetailDTO> response = homepageService.viewPublicActivityDetail(1L, 5);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getRelatedActivities().size());
    }
}

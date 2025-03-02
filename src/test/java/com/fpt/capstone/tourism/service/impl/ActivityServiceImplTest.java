package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.ActivityCategoryDTO;
import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ActivityCategoryMapper;
import com.fpt.capstone.tourism.mapper.ActivityMapper;
import com.fpt.capstone.tourism.mapper.GeoPositionMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.model.Activity;
import com.fpt.capstone.tourism.model.ActivityCategory;
import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private GeoPositionMapper geoPositionMapper;
    @Mock
    private ActivityCategoryMapper activityCategoryMapper;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private Activity activity;
    private ActivityDTO activityDTO;

    @BeforeEach
    void setUp() {
        activity = new Activity();
        activity.setId(1L);
        activity.setTitle("Hiking Tour");
        activity.setCreatedAt(LocalDateTime.now());
        activity.setDeleted(false);

        activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setTitle("Hiking Tour");
    }

    @Test
    void testFindRecommendedActivities() {
        when(activityRepository.findRandomActivities(1)).thenReturn(List.of(activity));
        when(activityMapper.toDTO(any())).thenReturn(activityDTO);

        List<ActivityDTO> result = activityService.findRecommendedActivities(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hiking Tour", result.get(0).getTitle());

        verify(activityRepository, times(1)).findRandomActivities(1);
        verify(activityMapper, times(1)).toDTO(any());
    }

    @Test
    void testSaveActivity_Success() {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setTitle("Hiking Tour");
        activityDTO.setContent("A fun hiking trip");
        activityDTO.setImageUrl("https://example.com/image.jpg");
        activityDTO.setPricePerPerson(100.0);
        activityDTO.setDeleted(false);

        GeoPositionDTO geoPositionDTO = new GeoPositionDTO();
        geoPositionDTO.setLatitude(37.7749);
        geoPositionDTO.setLongitude(-122.4194);
        activityDTO.setGeoPosition(geoPositionDTO);

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setName("San Francisco");
        activityDTO.setLocation(locationDTO);

        ActivityCategoryDTO activityCategoryDTO = new ActivityCategoryDTO();
        activityCategoryDTO.setId(1L);
        activityCategoryDTO.setName("Adventure");
        activityDTO.setActivityCategory(activityCategoryDTO);

        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle(activityDTO.getTitle());
        activity.setContent(activityDTO.getContent());
        activity.setImageUrl(activityDTO.getImageUrl());
        activity.setPricePerPerson(activityDTO.getPricePerPerson());
        activity.setDeleted(activityDTO.isDeleted());

        GeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(37.7749);
        geoPosition.setLongitude(-122.4194);
        activity.setGeoPosition(geoPosition);

        Location location = new Location();
        location.setId(1L);
        location.setName("San Francisco");
        activity.setLocation(location);

        ActivityCategory activityCategory = new ActivityCategory();
        activityCategory.setId(1L);
        activityCategory.setName("Adventure");
        activity.setActivityCategory(activityCategory);

        // Mock repository & mapper behaviors
        when(activityRepository.findByTitle(activityDTO.getTitle())).thenReturn(null);
        when(activityMapper.toEntity(any())).thenReturn(activity);
        when(activityMapper.toDTO(any())).thenReturn(activityDTO);
        when(activityRepository.save(any())).thenReturn(activity);

        // Call the service method
        var response = activityService.saveActivity(activityDTO);

        // Assertions to validate the success case
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Create activity successfully", response.getMessage());
        assertEquals(1L, response.getData().getId());

        // Verify repository interactions
        verify(activityRepository, times(1)).save(any());
    }

    @Test
    void testGetActivityById_Success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityMapper.toDTO(any())).thenReturn(activityDTO);

        var response = activityService.getActivityById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully", response.getMessage());
        assertEquals(1L, response.getData().getId());

        verify(activityRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllActivity_Success() {
        Page<Activity> activityPage = new PageImpl<>(List.of(activity), PageRequest.of(0, 5), 1);

        // Explicitly match Specification and Pageable
        when(activityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(activityPage);

        when(activityMapper.toDTO(any())).thenReturn(activityDTO);

        var response = activityService.getAllActivity(0, 5, "", false, "asc", null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, response.getData().getTotal());

        // Verify correct method call with expected arguments
        verify(activityRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testUpdateActivity_Success() {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setTitle("Updated Hiking Tour");
        activityDTO.setContent("An amazing hiking experience");
        activityDTO.setImageUrl("https://example.com/new-image.jpg");
        activityDTO.setPricePerPerson(150.0);
        activityDTO.setDeleted(false);

        GeoPositionDTO geoPositionDTO = new GeoPositionDTO();
        geoPositionDTO.setLatitude(40.7128);
        geoPositionDTO.setLongitude(-74.0060);
        activityDTO.setGeoPosition(geoPositionDTO);

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setId(2L);
        locationDTO.setName("New York");
        activityDTO.setLocation(locationDTO);

        ActivityCategoryDTO activityCategoryDTO = new ActivityCategoryDTO();
        activityCategoryDTO.setId(2L);
        activityCategoryDTO.setName("Extreme Sports");
        activityDTO.setActivityCategory(activityCategoryDTO);

        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("Hiking Tour");
        activity.setContent("A fun hiking trip");
        activity.setImageUrl("https://example.com/image.jpg");
        activity.setPricePerPerson(100.0);
        activity.setDeleted(false);

        GeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(37.7749);
        geoPosition.setLongitude(-122.4194);
        activity.setGeoPosition(geoPosition);

        Location location = new Location();
        location.setId(1L);
        location.setName("San Francisco");
        activity.setLocation(location);

        ActivityCategory activityCategory = new ActivityCategory();
        activityCategory.setId(1L);
        activityCategory.setName("Adventure");
        activity.setActivityCategory(activityCategory);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityRepository.findByTitle(activityDTO.getTitle())).thenReturn(null);
        when(geoPositionMapper.toEntity(any(GeoPositionDTO.class))).thenReturn(geoPosition);
        when(locationMapper.toEntity(any(LocationDTO.class))).thenReturn(location);
        when(activityCategoryMapper.toEntity(any(ActivityCategoryDTO.class))).thenReturn(activityCategory);
        when(activityRepository.save(any())).thenReturn(activity);
        when(activityMapper.toDTO(any())).thenReturn(activityDTO);

        var response = activityService.updateActivity(1L, activityDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully", response.getMessage());
        assertEquals(1L, response.getData().getId());

        verify(activityRepository, times(1)).save(any());
    }

    @Test
    void testDeleteActivity_Success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityMapper.toDTO(any())).thenReturn(activityDTO);

        var response = activityService.deleteActivity(1L, true);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertFalse(response.getData().isDeleted());

        verify(activityRepository, times(1)).save(any());
    }
}

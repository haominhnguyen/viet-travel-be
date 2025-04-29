package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.GeoPositionRequestDTO;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.GeoPositionMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private GeoPositionMapper geoPositionMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Location location;
    private LocationRequestDTO locationRequestDTO;
    private LocationDTO locationDTO;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1L);
        location.setName("Hanoi");
        location.setImage("test.jpg");
        location.setDeleted(false);
        location.setCreatedAt(LocalDateTime.now());

        GeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(21.0285);
        geoPosition.setLongitude(105.8542);
        location.setGeoPosition(geoPosition);

        locationRequestDTO = new LocationRequestDTO();
        locationRequestDTO.setName("Hanoi");
        locationRequestDTO.setDescription("Capital city of Vietnam");
        locationRequestDTO.setImage("test.jpg");

        GeoPositionRequestDTO geoPositionDTO = new GeoPositionRequestDTO();
        geoPositionDTO.setLatitude(21.0285);
        geoPositionDTO.setLongitude(105.8542);
        locationRequestDTO.setGeoPosition(geoPositionDTO);

        locationDTO = new LocationDTO();
        locationDTO.setId(1L);
        locationDTO.setName("Hanoi");
    }

    @Test
    void saveLocation_Success() {
        when(locationRepository.findByName("Hanoi")).thenReturn(null);
        when(locationMapper.toEntity(locationRequestDTO)).thenReturn(location);
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        when(locationMapper.toDTO(any(Location.class))).thenReturn(locationDTO);

        GeneralResponse<LocationDTO> response = locationService.saveLocation(locationRequestDTO);

        assertNotNull(response);
        assertEquals("Tạo địa điểm thành công", response.getMessage());
        assertEquals(1L, response.getData().getId());
    }

    @Test
    void saveLocation_ThrowsBusinessException_WhenLocationExists() {
        when(locationRepository.findByName("Hanoi")).thenReturn(location);

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            locationService.saveLocation(locationRequestDTO);
        });

        assertEquals("Địa điểm đã tồn tại", thrown.getMessage());
    }

    @Test
    void getLocationById_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationMapper.toDTO(any(Location.class))).thenReturn(locationDTO);

        GeneralResponse<LocationDTO> response = locationService.getLocationById(1L);

        assertNotNull(response);
        assertEquals("Thành công", response.getMessage());
        assertEquals("Hanoi", response.getData().getName());
    }

    @Test
    void getLocationById_ThrowsException_WhenNotFound() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> locationService.getLocationById(1L));
    }


    @Test
    void getAllLocation_Success() {
        Page<Location> locationPage = new PageImpl<>(Arrays.asList(location));
        when(locationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(locationPage);
        when(locationMapper.toDTO(any(Location.class))).thenReturn(locationDTO);

        GeneralResponse<PagingDTO<List<LocationDTO>>> response = locationService.getAllLocation(0, 10, "", false, "asc");

        assertNotNull(response);
        assertEquals(1, response.getData().getItems().size());
    }

    @Test
    void updateLocation_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationMapper.toDTO(any(Location.class))).thenReturn(locationDTO);

        GeneralResponse<LocationDTO> response = locationService.updateLocation(1L, locationRequestDTO);

        assertNotNull(response);
        assertEquals("Thành công", response.getMessage());
        assertEquals("Hanoi", response.getData().getName());
    }


    @Test
    void findRecommendedLocations_Success() {
        List<Location> locations = Arrays.asList(location);
        when(locationRepository.findRandomLocation(1)).thenReturn(locations);
        when(locationMapper.toPublicLocationDTO(any(Location.class))).thenReturn(new PublicLocationDTO());

        List<PublicLocationDTO> result = locationService.findRecommendedLocations(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

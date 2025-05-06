package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateRequestDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.repository.ServiceCategoryRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.TourDayRepository;
import com.fpt.capstone.tourism.repository.TourDayServiceCategoryRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourDayServiceIImplTest {

    @Mock
    private TourDayRepository tourDayRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private TourDayServiceCategoryRepository tourDayServiceCategoryRepository;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @InjectMocks
    private TourDayServiceIImpl tourDayService;

    private Tour tour;
    private TourDay tourDay;
    private Location location;
    private LocationDTO locationDTO;
    private List<TourDay> tourDays;
    private ServiceCategory serviceCategory;
    private List<TourDayServiceCategory> tourDayServiceCategories;
    private TourDayCreateRequestDTO createRequestDTO;
    private TourDayUpdateRequestDTO updateRequestDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        tour = new Tour();
        tour.setId(1L);
        tour.setName("Test Tour");
        tour.setNumberDays(3);
        tour.setNumberNights(2);

        location = new Location();
        location.setId(1L);
        location.setName("Test Location");

        locationDTO = LocationDTO.builder()
                .id(1L)
                .name("Test Location")
                .build();

        tourDay = new TourDay();
        tourDay.setId(1L);
        tourDay.setTitle("Day 1 - Welcome");
        tourDay.setDayNumber(1);
        tourDay.setContent("Welcome and introduction");
        tourDay.setMealPlan("Breakfast, Lunch, Dinner");
        tourDay.setTour(tour);
        tourDay.setLocation(location);
        tourDay.setDeleted(false);
        tourDay.setCreatedAt(LocalDateTime.now());
        tourDay.setUpdatedAt(LocalDateTime.now());

        tourDays = Collections.singletonList(tourDay);

        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);
        serviceCategory.setCategoryName("Hotel");

        TourDayServiceCategory tourDayServiceCategory = new TourDayServiceCategory();
        tourDayServiceCategory.setTourDay(tourDay);
        tourDayServiceCategory.setServiceCategory(serviceCategory);

        tourDayServiceCategories = Collections.singletonList(tourDayServiceCategory);

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
    void getTourDayDetail_WhenTourDaysExist_ReturnsTourDays() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdOrderByDayNumber(1L)).thenReturn(tourDays);
        when(tourDayServiceCategoryRepository.findByTourDay(tourDay)).thenReturn(tourDayServiceCategories);
        when(locationMapper.toDTO(location)).thenReturn(locationDTO);

        // Act
        GeneralResponse<List<TourDayFullDTO>> response = tourDayService.getTourDayDetail(1L, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("Tải chi tiết ngày tour thành công", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals(1L, response.getData().get(0).getId());
        assertEquals("Day 1 - Welcome", response.getData().get(0).getTitle());
        assertEquals(1, response.getData().get(0).getDayNumber());
        assertEquals(1L, response.getData().get(0).getTourId());
        assertEquals(false, response.getData().get(0).getDeleted());
        assertEquals(1, response.getData().get(0).getServiceCategories().size());
        assertEquals("Hotel", response.getData().get(0).getServiceCategories().get(0));

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdOrderByDayNumber(1L);
        verify(tourDayServiceCategoryRepository).findByTourDay(tourDay);
        verify(locationMapper).toDTO(location);
    }

    @Test
    void getTourDayDetail_WithDeletedFilter_ReturnsTourDays() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdAndDeletedOrderByDayNumber(1L, false)).thenReturn(tourDays);
        when(tourDayServiceCategoryRepository.findByTourDay(tourDay)).thenReturn(tourDayServiceCategories);
        when(locationMapper.toDTO(location)).thenReturn(locationDTO);

        // Act
        GeneralResponse<List<TourDayFullDTO>> response = tourDayService.getTourDayDetail(1L, false);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("Tải chi tiết ngày tour thành công", response.getMessage());
        assertEquals(1, response.getData().size());

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdAndDeletedOrderByDayNumber(1L, false);
        verify(tourDayServiceCategoryRepository).findByTourDay(tourDay);
    }

    @Test
    void getTourDayDetail_WhenTourNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.getTourDayDetail(999L, null);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertEquals("Không tìm thấy tour", exception.getMessage());

        verify(tourRepository).findById(999L);
        verify(tourDayRepository, never()).findByTourIdOrderByDayNumber(anyLong());
    }

    @Test
    void getTourDayDetail_WhenNoTourDaysFound_ReturnsEmptyList() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdOrderByDayNumber(1L)).thenReturn(Collections.emptyList());

        // Act
        GeneralResponse<List<TourDayFullDTO>> response = tourDayService.getTourDayDetail(1L, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("Không tìm thấy ngày tour nào cho tour này", response.getMessage());
        assertTrue(response.getData().isEmpty());

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdOrderByDayNumber(1L);
    }

    @Test
    void createTourDay_CreatesNewTourDay() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(serviceCategoryRepository.findByCategoryName("Hotel")).thenReturn(Optional.of(serviceCategory));

        ServiceCategory transportCategory = new ServiceCategory();
        transportCategory.setId(2L);
        transportCategory.setCategoryName("Transport");
        when(serviceCategoryRepository.findByCategoryName("Transport")).thenReturn(Optional.of(transportCategory));

        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Hotel")).thenReturn(true);
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Transport")).thenReturn(true);

        when(tourDayRepository.findMaxDayNumberByTourId(1L)).thenReturn(Optional.of(1));

        TourDay newTourDay = new TourDay();
        newTourDay.setId(2L);
        newTourDay.setDayNumber(2);
        newTourDay.setTitle("Day 2 - Exploration");
        newTourDay.setContent("City tour and exploration");
        newTourDay.setMealPlan("Breakfast, Lunch");
        newTourDay.setTour(tour);
        newTourDay.setLocation(location);
        newTourDay.setDeleted(false);

        when(tourDayRepository.save(any(TourDay.class))).thenReturn(newTourDay);
        when(locationMapper.toDTO(location)).thenReturn(locationDTO);

        // Act
        GeneralResponse<TourDayFullDTO> response = tourDayService.createTourDay(1L, createRequestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED.value(), response.getCode());
        assertEquals("Tạo ngày tour thành công", response.getMessage());
        assertEquals(2L, response.getData().getId());
        assertEquals("Day 2 - Exploration", response.getData().getTitle());
        assertEquals(2, response.getData().getDayNumber());
        assertEquals(2, response.getData().getServiceCategories().size());
        assertTrue(response.getData().getServiceCategories().contains("Hotel"));
        assertTrue(response.getData().getServiceCategories().contains("Transport"));

        verify(tourRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tourDayRepository).findMaxDayNumberByTourId(1L);
        verify(tourDayRepository).save(any(TourDay.class));
        verify(serviceCategoryRepository).findByCategoryName("Hotel");
        verify(serviceCategoryRepository).findByCategoryName("Transport");
        verify(tourDayServiceCategoryRepository, times(2)).save(any(TourDayServiceCategory.class));
    }

    @Test
    void createTourDay_WhenTourNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.createTourDay(999L, createRequestDTO);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertEquals("Không tìm thấy tour", exception.getMessage());

        verify(tourRepository).findById(999L);
        verify(tourDayRepository, never()).save(any(TourDay.class));
    }

    @Test
    void createTourDay_WhenLocationNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());
        createRequestDTO.setLocationId(999L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.createTourDay(1L, createRequestDTO);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertEquals("Không tìm thấy địa điểm", exception.getMessage());

        verify(tourRepository).findById(1L);
        verify(locationRepository).findById(999L);
        verify(tourDayRepository, never()).save(any(TourDay.class));
    }

    @Test
    void createTourDay_WhenNoServiceProviders_ThrowsException() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Hotel")).thenReturn(true);
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Transport")).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.createTourDay(1L, createRequestDTO);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertEquals("Không có nhà cung cấp dịch vụ cho danh mục tại địa điểm đã chọn.", exception.getMessage());

        verify(tourRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(serviceProviderRepository).existsByLocationIdAndCategoryName(1L, "Hotel");
        verify(serviceProviderRepository).existsByLocationIdAndCategoryName(1L, "Transport");
        verify(tourDayRepository, never()).save(any(TourDay.class));
    }

    @Test
    void createTourDay_WhenExceedingMaxDays_ThrowsException() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Hotel")).thenReturn(true);
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Transport")).thenReturn(true);
        when(tourDayRepository.findMaxDayNumberByTourId(1L)).thenReturn(Optional.of(3)); // Max day is already equal to tour.getNumberDays()

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.createTourDay(1L, createRequestDTO);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertEquals("Không thể tạo thêm ngày vì đã vượt quá số ngày/đêm tối đa được định nghĩa trong tour: 3 ngày/đêm", exception.getMessage());

        verify(tourRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tourDayRepository).findMaxDayNumberByTourId(1L);
        verify(tourDayRepository, never()).save(any(TourDay.class));
    }

    @Test
    void updateTourDay_UpdatesExistingTourDay() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByIdAndTourId(1L, 1L)).thenReturn(Optional.of(tourDay));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        when(serviceCategoryRepository.findByCategoryName("Hotel")).thenReturn(Optional.of(serviceCategory));

        ServiceCategory transportCategory = new ServiceCategory();
        transportCategory.setId(2L);
        transportCategory.setCategoryName("Transport");
        when(serviceCategoryRepository.findByCategoryName("Transport")).thenReturn(Optional.of(transportCategory));

        ServiceCategory activityCategory = new ServiceCategory();
        activityCategory.setId(3L);
        activityCategory.setCategoryName("Activity");
        when(serviceCategoryRepository.findByCategoryName("Activity")).thenReturn(Optional.of(activityCategory));

        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Hotel")).thenReturn(true);
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Transport")).thenReturn(true);
        when(serviceProviderRepository.existsByLocationIdAndCategoryName(1L, "Activity")).thenReturn(true);

        when(tourDayRepository.save(any(TourDay.class))).thenReturn(tourDay);
        when(locationMapper.toDTO(location)).thenReturn(locationDTO);

        // Act
        GeneralResponse<TourDayFullDTO> response = tourDayService.updateTourDay(1L, 1L, updateRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("Cập nhật ngày tour thành công", response.getMessage());
        assertEquals(1L, response.getData().getId());
        assertEquals("Day 1 - Updated", response.getData().getTitle());
        assertEquals("Updated content", response.getData().getContent());
        assertEquals("Breakfast, Dinner", response.getData().getMealPlan());
        assertEquals(3, response.getData().getServiceCategories().size());

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByIdAndTourId(1L, 1L);
        verify(locationRepository).findById(1L);
        verify(tourDayRepository).save(tourDay);
        verify(tourDayServiceCategoryRepository).deleteByTourDay(tourDay);
        verify(tourDayServiceCategoryRepository, times(3)).save(any(TourDayServiceCategory.class));
    }

    @Test
    void updateTourDay_WhenTourDayNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByIdAndTourId(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.updateTourDay(999L, 1L, updateRequestDTO);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertEquals("Không tìm thấy ngày tour", exception.getMessage());

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByIdAndTourId(999L, 1L);
        verify(tourDayRepository, never()).save(any(TourDay.class));
    }

    @Test
    void changeTourDayStatus_ChangesStatus() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByIdAndTourId(1L, 1L)).thenReturn(Optional.of(tourDay));
        when(tourDayRepository.save(any(TourDay.class))).thenReturn(tourDay);

        // Act
        GeneralResponse<String> response = tourDayService.changeTourDayStatus(1L, 1L, true);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("Xóa ngày tour thành công", response.getMessage());
        assertEquals("Ngày tour có ID 1 đã được xoá thành công.", response.getData());
        assertTrue(tourDay.getDeleted());

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByIdAndTourId(1L, 1L);
        verify(tourDayRepository).save(tourDay);
    }

    @Test
    void changeTourDayStatus_WhenTourDayNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByIdAndTourId(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDayService.changeTourDayStatus(999L, 1L, true);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertEquals("Không tìm thấy ngày tour", exception.getMessage());

        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByIdAndTourId(999L, 1L);
        verify(tourDayRepository, never()).save(any(TourDay.class));
    }
}
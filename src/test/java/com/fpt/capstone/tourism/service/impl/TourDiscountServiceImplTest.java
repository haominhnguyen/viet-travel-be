package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Meal;
import com.fpt.capstone.tourism.model.Room;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.ServicePaxPricing;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.TourDayService;
import com.fpt.capstone.tourism.model.TourPax;
import com.fpt.capstone.tourism.model.Transport;
import com.fpt.capstone.tourism.model.enums.MealType;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.repository.MealRepository;
import com.fpt.capstone.tourism.repository.RoomRepository;
import com.fpt.capstone.tourism.repository.ServiceCategoryRepository;
import com.fpt.capstone.tourism.repository.ServicePaxPricingRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.repository.TourDayRepository;
import com.fpt.capstone.tourism.repository.TourDayServiceRepository;
import com.fpt.capstone.tourism.repository.TourPaxRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TransportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourDiscountServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourDayRepository tourDayRepository;

    @Mock
    private TourDayServiceRepository tourDayServiceRepository;

    @Mock
    private TourPaxRepository tourPaxRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private ServicePaxPricingRepository servicePaxPricingRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TourDayService> typedQuery;

    @InjectMocks
    private TourDiscountServiceImpl tourDiscountService;

    private Tour tour;
    private TourDay tourDay;
    private Service service;
    private ServiceCategory serviceCategory;
    private ServiceProvider serviceProvider;
    private Location location;
    private TourDayService tourDayService;
    private TourPax tourPax;
    private ServicePaxPricing servicePaxPricing;
    private Room room;
    private Meal meal;
    private Transport transport;

    @BeforeEach
    void setUp() {
        // Setup basic test data
        tour = new Tour();
        tour.setId(1L);
        tour.setName("Test Tour");
        tour.setTourType(TourType.SIC);
        tour.setNumberDays(3);
        tour.setNumberNights(2);

        tourDay = new TourDay();
        tourDay.setId(1L);
        tourDay.setDayNumber(1);
        tourDay.setTitle("Day 1");
        tourDay.setTour(tour);

        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);
        serviceCategory.setCategoryName("Hotel");

        serviceProvider = new ServiceProvider();
        serviceProvider.setId(1L);
        serviceProvider.setName("Hotel Provider");

        location = new Location();
        location.setId(1L);
        location.setName("City Center");

        tourDay.setLocation(location);

        service = new Service();
        service.setId(1L);
        service.setName("Luxury Hotel");
        service.setNettPrice(100.0);
        service.setSellingPrice(150.0);
        service.setStartDate(LocalDateTime.now().minusDays(10));
        service.setEndDate(LocalDateTime.now().plusDays(30));
        service.setServiceCategory(serviceCategory);
        service.setServiceProvider(serviceProvider);

        tourDayService = new TourDayService();
        tourDayService.setId(1L);
        tourDayService.setTourDay(tourDay);
        tourDayService.setService(service);
        tourDayService.setQuantity(1);
        tourDayService.setSellingPrice(150.0);

        tourPax = new TourPax();
        tourPax.setId(1L);
        tourPax.setTour(tour);
        tourPax.setMinPax(1);
        tourPax.setMaxPax(2);
        tourPax.setNettPricePerPax(80.0);
        tourPax.setSellingPrice(120.0);
        tourPax.setFixedCost(50.0);
        tourPax.setExtraHotelCost(20.0);
        tourPax.setDeleted(false);

        servicePaxPricing = new ServicePaxPricing();
        servicePaxPricing.setId(1L);
        servicePaxPricing.setTourDayService(tourDayService);
        servicePaxPricing.setTourPax(tourPax);
        servicePaxPricing.setSellingPrice(120.0);
        servicePaxPricing.setDeleted(false);

        room = new Room();
        room.setId(1L);
        room.setService(service);
        room.setCapacity(2);
        room.setAvailableQuantity(10);
        room.setFacilities("WiFi, AC, TV");
        room.setDeleted(false);

        meal = new Meal();
        meal.setId(1L);
        meal.setService(service);
        meal.setType(MealType.BREAKFAST);
        meal.setMealDetail("Continental breakfast");
        meal.setDeleted(false);

        transport = new Transport();
        transport.setId(1L);
        transport.setService(service);
        transport.setSeatCapacity(40);
        transport.setDeleted(false);
    }

    @Test
    void getTourServicesList_ReturnsServicesSuccessfully() {
        // Arrange
        List<TourDay> tourDays = Collections.singletonList(tourDay);
        List<TourDayService> tourDayServices = Collections.singletonList(tourDayService);
        List<Long> tourDayIds = Collections.singletonList(1L);
        List<Long> tourDayServiceIds = Collections.singletonList(1L);
        List<ServicePaxPricing> servicePaxPricings = Collections.singletonList(servicePaxPricing);
        List<TourPax> tourPaxes = Collections.singletonList(tourPax);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
        when(tourDayServiceRepository.findByTourDayIdIn(tourDayIds)).thenReturn(tourDayServices);
        when(servicePaxPricingRepository.findByTourDayServiceIdInAndDeletedFalse(tourDayServiceIds))
                .thenReturn(servicePaxPricings);
        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L)).thenReturn(tourPaxes);

        // Act
        GeneralResponse<TourServiceListDTO> response = tourDiscountService.getTourServicesList(1L, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getTourId());
        assertEquals("Test Tour", response.getData().getTourName());
        assertEquals("SIC", response.getData().getTourType());
        assertEquals(1, response.getData().getTotalDays());
        assertFalse(response.getData().getServiceCategories().isEmpty());
        assertFalse(response.getData().getPaxOptions().isEmpty());

        // Verify repository calls
        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdAndDeletedFalseOrderByDayNumber(1L);
        verify(tourDayServiceRepository).findByTourDayIdIn(tourDayIds);
        verify(servicePaxPricingRepository).findByTourDayServiceIdInAndDeletedFalse(tourDayServiceIds);
        verify(tourPaxRepository).findByTourIdAndDeletedFalseOrderByMinPax(1L);
    }

    @Test
    void getTourServicesList_WithPaxCount_ReturnsFilteredServices() {
        // Arrange
        List<TourDay> tourDays = Collections.singletonList(tourDay);
        List<TourDayService> tourDayServices = Collections.singletonList(tourDayService);
        List<Long> tourDayIds = Collections.singletonList(1L);
        List<Long> tourDayServiceIds = Collections.singletonList(1L);
        List<ServicePaxPricing> servicePaxPricings = Collections.singletonList(servicePaxPricing);
        List<TourPax> tourPaxes = Collections.singletonList(tourPax);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
        when(tourDayServiceRepository.findByTourDayIdIn(tourDayIds)).thenReturn(tourDayServices);
        when(servicePaxPricingRepository.findByTourDayServiceIdInAndDeletedFalse(tourDayServiceIds))
                .thenReturn(servicePaxPricings);
        when(tourPaxRepository.findByTourIdAndPaxRangeNonDeleted(1L, 2)).thenReturn(tourPaxes);

        // Act
        GeneralResponse<TourServiceListDTO> response = tourDiscountService.getTourServicesList(1L, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());

        // Verify repository calls
        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdAndDeletedFalseOrderByDayNumber(1L);
        verify(tourDayServiceRepository).findByTourDayIdIn(tourDayIds);
        verify(servicePaxPricingRepository).findByTourDayServiceIdInAndDeletedFalse(tourDayServiceIds);
        verify(tourPaxRepository).findByTourIdAndPaxRangeNonDeleted(1L, 2);
    }

    @Test
    void getTourServicesList_TourNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDiscountService.getTourServicesList(999L, null);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertFalse(exception.getMessage().contains("Tour not found"));

        // Verify repository calls
        verify(tourRepository).findById(999L);
        verifyNoMoreInteractions(tourDayRepository, tourDayServiceRepository);
    }

    @Test
    void getServiceDetailByDayAndService_ReturnsServiceDetail() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdAndDayNumber(1L, 1)).thenReturn(Optional.of(tourDay));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(tourDayServiceRepository.findByTourDayIdAndServiceId(1L, 1L)).thenReturn(Optional.of(tourDayService));
        when(servicePaxPricingRepository.findByTourDayServiceIdAndDeletedFalse(1L))
                .thenReturn(Collections.singletonList(servicePaxPricing));
        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L))
                .thenReturn(Collections.singletonList(tourPax));
        when(roomRepository.findByServiceId(1L)).thenReturn(Optional.of(room));

        // Act
        GeneralResponse<ServiceByCategoryDTO> response = tourDiscountService.getServiceDetailByDayAndService(1L, 1, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        assertEquals("Luxury Hotel", response.getData().getName());
        assertEquals(1, response.getData().getDayNumber());
        assertEquals("ACTIVE", response.getData().getStatus());
        assertEquals("Hotel", response.getData().getCategoryName());
        assertNotNull(response.getData().getRoomDetail());
        assertEquals(2, response.getData().getRoomDetail().getCapacity());

        // Verify repository calls
        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdAndDayNumber(1L, 1);
        verify(serviceRepository).findById(1L);
        verify(tourDayServiceRepository).findByTourDayIdAndServiceId(1L, 1L);
        verify(servicePaxPricingRepository).findByTourDayServiceIdAndDeletedFalse(1L);
        verify(tourPaxRepository).findByTourIdAndDeletedFalseOrderByMinPax(1L);
        verify(roomRepository).findByServiceId(1L);
    }

    @Test
    void getServiceDetailByDayAndService_ServiceNotFound_ThrowsException() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourDayRepository.findByTourIdAndDayNumber(1L, 1)).thenReturn(Optional.of(tourDay));
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourDiscountService.getServiceDetailByDayAndService(1L, 1, 999L);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertFalse(exception.getMessage().contains("Service not found"));

        // Verify repository calls
        verify(tourRepository).findById(1L);
        verify(tourDayRepository).findByTourIdAndDayNumber(1L, 1);
        verify(serviceRepository).findById(999L);
    }

    @Test
    void getDayNumbersByServiceAndTour_ReturnsDayNumbers() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(tourDayServiceRepository.findByServiceIdAndTourDayTourId(1L, 1L))
                .thenReturn(Optional.of(tourDayService));

        // Act
        GeneralResponse<List<Integer>> response = tourDiscountService.getDayNumbersByServiceAndTour(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(1, response.getData().get(0));

        // Verify repository calls
        verify(tourRepository).findById(1L);
        verify(serviceRepository).findById(1L);
        verify(tourDayServiceRepository).findByServiceIdAndTourDayTourId(1L, 1L);
    }

    @Test
    void getServicesByProviderAndCategory_ReturnsServices() {
        // Arrange
        when(serviceProviderRepository.findById(1L)).thenReturn(Optional.of(serviceProvider));
        when(serviceCategoryRepository.findByCategoryName("Hotel")).thenReturn(Optional.of(serviceCategory));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(serviceRepository.findByServiceCategoryNameAndProviderIdAndLocationId("Hotel", 1L, 1L))
                .thenReturn(Collections.singletonList(service));
        when(roomRepository.findByServiceIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));

        // Act
        GeneralResponse<ServiceProviderServicesDTO> response =
                tourDiscountService.getServicesByProviderAndCategory(1L, "Hotel", 1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getProviderId());
        assertEquals("Hotel Provider", response.getData().getProviderName());
        assertEquals("Hotel", response.getData().getCategoryName());
        assertEquals(1, response.getData().getAvailableServices().size());

        AvailableServiceDTO serviceDTO = response.getData().getAvailableServices().get(0);
        assertEquals(1L, serviceDTO.getId());
        assertEquals("Luxury Hotel", serviceDTO.getName());
        assertEquals("Hotel", serviceDTO.getCategoryName());
        assertNotNull(serviceDTO.getRoomDetail());
        assertEquals(2, serviceDTO.getRoomDetail().getCapacity());

        // Verify repository calls
        verify(serviceProviderRepository).findById(1L);
        verify(serviceCategoryRepository).findByCategoryName("Hotel");
        verify(locationRepository).findById(1L);
        verify(serviceRepository).findByServiceCategoryNameAndProviderIdAndLocationId("Hotel", 1L, 1L);
        verify(roomRepository).findByServiceIdAndDeletedFalse(1L);
    }

    @Test
    void getServiceProviderOptions_ReturnsProviderOptions() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(serviceCategoryRepository.findByCategoryName("Hotel")).thenReturn(Optional.of(serviceCategory));

        List<ServiceProvider> providers = Collections.singletonList(serviceProvider);
        when(serviceProviderRepository.findByLocationIdAndServiceCategoryId(1L, 1L)).thenReturn(providers);

        // Act
        GeneralResponse<ServiceProviderOptionsDTO> response =
                tourDiscountService.getServiceProviderOptions(1L, "Hotel");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getLocationId());
        assertEquals("City Center", response.getData().getLocationName());
        assertEquals("Hotel", response.getData().getCategoryName());
        assertEquals(1, response.getData().getServiceProviders().size());

        ServiceProviderOptionDTO providerDTO = response.getData().getServiceProviders().get(0);
        assertEquals(1L, providerDTO.getId());
        assertEquals("Hotel Provider", providerDTO.getName());

        // Verify repository calls
        verify(locationRepository).findById(1L);
        verify(serviceCategoryRepository).findByCategoryName("Hotel");
        verify(serviceProviderRepository).findByLocationIdAndServiceCategoryId(1L, 1L);
    }

//    @Test
//    void createServiceDetail_CreatesServiceSuccessfully() {
//        // Arrange
//        ServiceCreateRequestDTO requestDTO = new ServiceCreateRequestDTO();
//        requestDTO.setServiceId(1L);
//        requestDTO.setDayNumber(1);
//        requestDTO.setSellingPrice(150.0);
//
//        Map<String, Double> paxPrices = new HashMap<>();
//        paxPrices.put("1", 120.0);
//        requestDTO.setPaxPrices(paxPrices);
//
//        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
//        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
//        when(tourDayRepository.findByTourIdAndDayNumber(1L, 1)).thenReturn(Optional.of(tourDay));
//
//        when(entityManager.createQuery(anyString(), eq(TourDayService.class))).thenReturn(typedQuery);
//        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
//        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
//
//        when(tourDayServiceRepository.save(any(TourDayService.class))).thenReturn(tourDayService);
//        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(tourPax));
//        when(servicePaxPricingRepository.save(any(ServicePaxPricing.class))).thenReturn(servicePaxPricing);
//
//        // Mock the second call to getServiceDetail
//        when(tourDayServiceRepository.findByServiceIdAndTourDayTourId(1L, 1L))
//                .thenReturn(Optional.of(tourDayService));
//        when(servicePaxPricingRepository.findByTourDayServiceIdAndDeletedFalse(1L))
//                .thenReturn(Collections.singletonList(servicePaxPricing));
//        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L))
//                .thenReturn(Collections.singletonList(tourPax));
//        when(roomRepository.findByServiceId(1L)).thenReturn(Optional.of(room));
//
//        // Act
//        GeneralResponse<ServiceByCategoryDTO> response = tourDiscountService.createServiceDetail(1L, requestDTO);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK.value(), response.getCode());
//        assertNotNull(response.getData());
//
//        // Verify repository calls
//        verify(tourRepository).findById(1L);
//        verify(serviceRepository).findById(1L);
//        verify(tourDayRepository).findByTourIdAndDayNumber(1L, 1);
//        verify(tourDayServiceRepository).save(any(TourDayService.class));
//        verify(tourPaxRepository).findById(1L);
//        verify(servicePaxPricingRepository).save(any(ServicePaxPricing.class));
//    }

//    @Test
//    void updateServiceDetail_UpdatesServiceSuccessfully() {
//        // Arrange
//        ServiceUpdateRequestDTO requestDTO = new ServiceUpdateRequestDTO();
//        requestDTO.setDayNumber(1);
//        requestDTO.setSellingPrice(160.0);
//
//        Map<Long, Double> paxPrices = new HashMap<>();
//        paxPrices.put(1L, 130.0);
//        requestDTO.setPaxPrices(paxPrices);
//
//        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
//        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
//
//        List<TourDayService> tourDayServices = Collections.singletonList(tourDayService);
//        when(entityManager.createQuery(anyString(), eq(TourDayService.class))).thenReturn(typedQuery);
//        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
//        when(typedQuery.getResultList()).thenReturn(tourDayServices);
//
//        when(tourDayRepository.findByTourIdAndDayNumber(1L, 1)).thenReturn(Optional.of(tourDay));
//        when(tourDayServiceRepository.save(any(TourDayService.class))).thenReturn(tourDayService);
//
//        List<ServicePaxPricing> pricings = Collections.singletonList(servicePaxPricing);
//        when(servicePaxPricingRepository.findByTourDayServiceId(1L)).thenReturn(pricings);
//        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(tourPax));
//        when(servicePaxPricingRepository.save(any(ServicePaxPricing.class))).thenReturn(servicePaxPricing);
//
//        when(roomRepository.findByServiceId(1L)).thenReturn(Optional.of(room));
//
//        // Mock the second call to getServiceDetail
//        when(tourDayServiceRepository.findByServiceIdAndTourDayTourId(1L, 1L))
//                .thenReturn(Optional.of(tourDayService));
//        when(servicePaxPricingRepository.findByTourDayServiceIdAndDeletedFalse(1L))
//                .thenReturn(Collections.singletonList(servicePaxPricing));
//        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L))
//                .thenReturn(Collections.singletonList(tourPax));
//
//        // Act
//        GeneralResponse<ServiceByCategoryDTO> response = tourDiscountService.updateServiceDetail(1L, 1L, requestDTO);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK.value(), response.getCode());
//        assertNotNull(response.getData());
//
//        // Verify repository calls
//        verify(tourRepository).findById(1L);
//        verify(serviceRepository).findById(1L);
//        verify(entityManager).createQuery(anyString(), eq(TourDayService.class));
//        verify(tourDayServiceRepository).save(any(TourDayService.class));
//        verify(servicePaxPricingRepository).findByTourDayServiceId(1L);
//        verify(servicePaxPricingRepository).save(any(ServicePaxPricing.class));
//    }

//    @Test
//    void removeServiceFromTour_RemovesServiceSuccessfully() {
//        // Arrange
//        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
//        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
//        when(tourDayRepository.findByTourIdAndDayNumber(1L, 1)).thenReturn(Optional.of(tourDay));
//
//        List<TourDayService> tourDayServices = Collections.singletonList(tourDayService);
//        when(entityManager.createQuery(anyString(), eq(TourDayService.class))).thenReturn(typedQuery);
//        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
//        when(typedQuery.getResultList()).thenReturn(tourDayServices);
//
//        List<ServicePaxPricing> pricings = Collections.singletonList(servicePaxPricing);
//        when(servicePaxPricingRepository.findByTourDayServiceId(1L)).thenReturn(pricings);
//
//        // Act
//        GeneralResponse<Void> response = tourDiscountService.removeServiceFromTour(1L, 1L, 1);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK.value(), response.getCode());
//        assertTrue(response.getMessage().contains("successfully removed"));
//
//        // Verify repository calls
//        verify(tourRepository).findById(1L);
//        verify(serviceRepository).findById(1L);
//        verify(tourDayRepository).findByTourIdAndDayNumber(1L, 1);
//        verify(entityManager).createQuery(anyString(), eq(TourDayService.class));
//        verify(servicePaxPricingRepository).findByTourDayServiceId(1L);
//        verify(servicePaxPricingRepository).deleteAll(pricings);
//        verify(tourDayServiceRepository).deleteAll(tourDayServices);
//        verify(entityManager).flush();
//    }
}
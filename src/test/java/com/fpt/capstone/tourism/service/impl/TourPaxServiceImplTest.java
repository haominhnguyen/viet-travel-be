package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayServicePricingDTO;
import com.fpt.capstone.tourism.dto.common.TourPaxFullDTO;
import com.fpt.capstone.tourism.dto.request.ServicePricingRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxUpdateRequestDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPaxServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourPaxRepository tourPaxRepository;

    @Mock
    private TourDayRepository tourDayRepository;

    @Mock
    private TourDayServiceRepository tourDayServiceRepository;

    @Mock
    private ServicePaxPricingRepository servicePaxPricingRepository;

    @InjectMocks
    private TourPaxServiceImpl tourPaxService;

    // Test data
    private Tour tour;
    private TourPax tourPax;
    private List<TourDay> tourDays;
    private List<TourDayService> tourDayServices;
    private List<ServicePaxPricing> servicePaxPricings;
    private TourPaxCreateRequestDTO createRequestDTO;
    private TourPaxUpdateRequestDTO updateRequestDTO;
    private Date currentDate;
    private Date futureDate;

    @BeforeEach
    void setUp() {
        // Initialize current and future dates
        currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        futureDate = calendar.getTime();

        // Create tour
        tour = new Tour();
        tour.setId(1L);
        tour.setName("Test Tour");

        // Create tour pax
        tourPax = new TourPax();
        tourPax.setId(1L);
        tourPax.setTour(tour);
        tourPax.setMinPax(2);
        tourPax.setMaxPax(10);
        tourPax.setFixedCost(500.0);
        tourPax.setExtraHotelCost(100.0);
        tourPax.setNettPricePerPax(200.0);
        tourPax.setSellingPrice(300.0);
        tourPax.setValidFrom(currentDate);
        tourPax.setValidTo(futureDate);
        tourPax.setDeleted(false);

        // Create tour days
        tourDays = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TourDay tourDay = new TourDay();
            tourDay.setId((long) i);
            tourDay.setTour(tour);
            tourDay.setDayNumber(i);
            tourDay.setTitle("Day " + i);
            tourDay.setContent("Content for day " + i);
            tourDay.setDeleted(false);
            tourDays.add(tourDay);
        }

        // Create services
        Service service1 = new Service();
        service1.setId(1L);
        service1.setName("Service 1");
        service1.setNettPrice(100.0);

        Service service2 = new Service();
        service2.setId(2L);
        service2.setName("Service 2");
        service2.setNettPrice(150.0);

        // Create service categories
        ServiceCategory category = new ServiceCategory();
        category.setId(1L);
        category.setCategoryName("Test Category");
        service1.setServiceCategory(category);
        service2.setServiceCategory(category);

        // Create tour day services
        tourDayServices = new ArrayList<>();
        TourDayService tds1 = new TourDayService();
        tds1.setId(1L);
        tds1.setTourDay(tourDays.get(0));
        tds1.setService(service1);

        TourDayService tds2 = new TourDayService();
        tds2.setId(2L);
        tds2.setTourDay(tourDays.get(1));
        tds2.setService(service2);

        tourDayServices.add(tds1);
        tourDayServices.add(tds2);

        // Create service pax pricings
        servicePaxPricings = new ArrayList<>();
        ServicePaxPricing spp1 = new ServicePaxPricing();
        spp1.setId(1L);
        spp1.setTourPax(tourPax);
        spp1.setTourDayService(tds1);
        spp1.setDeleted(false);

        ServicePaxPricing spp2 = new ServicePaxPricing();
        spp2.setId(2L);
        spp2.setTourPax(tourPax);
        spp2.setTourDayService(tds2);
        spp2.setDeleted(false);

        servicePaxPricings.add(spp1);
        servicePaxPricings.add(spp2);

        // Create request DTOs
        createRequestDTO = TourPaxCreateRequestDTO.builder()
                .minPax(2)
                .maxPax(10)
                .fixedCost(500.0)
                .extraHotelCost(100.0)
                .nettPricePerPax(200.0)
                .sellingPrice(300.0)
                .validFrom(currentDate)
                .validTo(futureDate)
                .servicePricings(new ArrayList<>())
                .build();

        updateRequestDTO = TourPaxUpdateRequestDTO.builder()
                .minPax(3)
                .maxPax(12)
                .fixedCost(550.0)
                .extraHotelCost(120.0)
                .nettPricePerPax(220.0)
                .sellingPrice(330.0)
                .validFrom(currentDate)
                .validTo(futureDate)
                .servicePricings(Arrays.asList(
                        ServicePricingRequestDTO.builder().tourDayServiceId(1L).customServicePrice(110.0).build(),
                        ServicePricingRequestDTO.builder().tourDayServiceId(2L).customServicePrice(160.0).build()
                ))
                .build();
    }

    @Test
    void getTourPaxConfiguration_Success() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(tourPax));
        when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);

        List<Long> tourDayIds = tourDays.stream().map(TourDay::getId).collect(Collectors.toList());
        when(tourDayServiceRepository.findByTourDayIdIn(tourDayIds)).thenReturn(tourDayServices);
        when(servicePaxPricingRepository.findByTourPaxIdAndDeletedFalse(1L)).thenReturn(servicePaxPricings);

        // Act
        GeneralResponse<TourPaxFullDTO> response = tourPaxService.getTourPaxConfiguration(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        assertEquals(1L, response.getData().getTourId());
        assertEquals(2, response.getData().getMinPax());
        assertEquals(10, response.getData().getMaxPax());
        assertEquals("2-10", response.getData().getPaxRange());
        assertEquals(500.0, response.getData().getFixedCost());
        assertEquals(100.0, response.getData().getExtraHotelCost());
        assertEquals(200.0, response.getData().getNettPricePerPax());
        assertEquals(300.0, response.getData().getSellingPrice());
        assertTrue(response.getData().isValid());
        assertNotNull(response.getData().getServiceAssociations());
        assertEquals(2, response.getData().getServiceAssociations().size());
    }

    @Test
    void getTourPaxConfiguration_TourNotFound() {
        // Arrange
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.getTourPaxConfiguration(999L, 1L);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("Tour not found"));
    }

    @Test
    void getTourPaxConfiguration_PaxNotFound() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.getTourPaxConfiguration(1L, 999L);
        });

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("Pax configuration not found"));
    }

    @Test
    void getTourPaxConfiguration_PaxNotAssociatedWithTour() {
        // Arrange
        Tour anotherTour = new Tour();
        anotherTour.setId(2L);

        TourPax paxFromAnotherTour = new TourPax();
        paxFromAnotherTour.setId(1L);
        paxFromAnotherTour.setTour(anotherTour);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(paxFromAnotherTour));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.getTourPaxConfiguration(1L, 1L);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("associated"));
    }

    @Test
    void createTourPaxConfiguration_Success() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L)).thenReturn(Collections.emptyList());
        when(tourPaxRepository.save(any(TourPax.class))).thenReturn(tourPax);
        when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);

        List<Long> tourDayIds = tourDays.stream().map(TourDay::getId).collect(Collectors.toList());
        when(tourDayServiceRepository.findByTourDayIdIn(tourDayIds)).thenReturn(tourDayServices);
        when(servicePaxPricingRepository.save(any(ServicePaxPricing.class)))
                .thenAnswer(invocation -> {
                    ServicePaxPricing spp = invocation.getArgument(0);
                    spp.setId(new Random().nextLong()); // Assign a random ID
                    return spp;
                });

        // Act
        GeneralResponse<TourPaxFullDTO> response = tourPaxService.createTourPaxConfiguration(1L, createRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        assertEquals(1L, response.getData().getTourId());
        assertEquals(2, response.getData().getMinPax());
        assertEquals(10, response.getData().getMaxPax());
        assertEquals("2-10", response.getData().getPaxRange());
        assertEquals(500.0, response.getData().getFixedCost());
        assertEquals(100.0, response.getData().getExtraHotelCost());
        assertEquals(200.0, response.getData().getNettPricePerPax());
        assertEquals(300.0, response.getData().getSellingPrice());
        assertTrue(response.getData().isValid());
        assertNotNull(response.getData().getServiceAssociations());
        assertEquals(2, response.getData().getServiceAssociations().size());

        // Verify repository calls
        verify(tourPaxRepository).save(any(TourPax.class));
        verify(servicePaxPricingRepository, times(2)).save(any(ServicePaxPricing.class));
    }

    @Test
    void createTourPaxConfiguration_InvalidPaxRange() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        // Modify request to have invalid range (min > max)
        TourPaxCreateRequestDTO invalidRequest = createRequestDTO.builder()
                .minPax(15)
                .maxPax(10)
                .build();

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.createTourPaxConfiguration(1L, invalidRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("pax"));
    }

    @Test
    void createTourPaxConfiguration_InvalidDateRange() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        // Modify request to have invalid date range (from > to)
        TourPaxCreateRequestDTO invalidRequest = createRequestDTO.builder()
                .validFrom(futureDate)
                .validTo(currentDate)
                .build();

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.createTourPaxConfiguration(1L, invalidRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("valid"));
    }

    @Test
    void createTourPaxConfiguration_OverlappingConfiguration() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        // Create an existing pax configuration that would overlap
        List<TourPax> existingPaxes = new ArrayList<>();
        existingPaxes.add(tourPax);

        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L)).thenReturn(existingPaxes);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.createTourPaxConfiguration(1L, createRequestDTO);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("overlaps"));
    }

    @Test
    void updateTourPaxConfiguration_Success() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(tourPax));
        when(tourPaxRepository.findByTourIdAndIdNotAndDeletedFalseOrderByMinPax(eq(1L), eq(1L)))
                .thenReturn(Collections.emptyList());
        when(tourPaxRepository.save(any(TourPax.class))).thenReturn(tourPax);
        when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);

        List<Long> tourDayIds = tourDays.stream().map(TourDay::getId).collect(Collectors.toList());
        when(tourDayServiceRepository.findByTourDayIdIn(tourDayIds)).thenReturn(tourDayServices);
        when(servicePaxPricingRepository.findByTourPaxId(1L)).thenReturn(servicePaxPricings);
        when(servicePaxPricingRepository.save(any(ServicePaxPricing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GeneralResponse<TourPaxFullDTO> response = tourPaxService.updateTourPaxConfiguration(1L, 1L, updateRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        assertEquals(1L, response.getData().getTourId());
        assertEquals(3, response.getData().getMinPax()); // Updated value
        assertEquals(12, response.getData().getMaxPax()); // Updated value
        assertEquals("3-12", response.getData().getPaxRange());
        assertEquals(550.0, response.getData().getFixedCost()); // Updated value
        assertEquals(120.0, response.getData().getExtraHotelCost()); // Updated value
        assertEquals(220.0, response.getData().getNettPricePerPax()); // Updated value
        assertEquals(330.0, response.getData().getSellingPrice()); // Updated value
        assertTrue(response.getData().isValid());
        assertNotNull(response.getData().getServiceAssociations());
        assertEquals(2, response.getData().getServiceAssociations().size());

        // Verify repository calls
        verify(tourPaxRepository).save(any(TourPax.class));
        verify(servicePaxPricingRepository, atLeastOnce()).save(any(ServicePaxPricing.class));
    }

    @Test
    void updateTourPaxConfiguration_InvalidPaxRange() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(tourPax));

        // Modify request to have invalid range (min > max)
        TourPaxUpdateRequestDTO invalidRequest = updateRequestDTO.builder()
                .minPax(15)
                .maxPax(10)
                .build();

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tourPaxService.updateTourPaxConfiguration(1L, 1L, invalidRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
        assertFalse(exception.getResponseMessage().contains("max"));
    }

    @Test
    void deleteTourPaxConfiguration_Success() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourPaxRepository.findById(1L)).thenReturn(Optional.of(tourPax));
        when(servicePaxPricingRepository.findByTourPaxId(1L)).thenReturn(servicePaxPricings);
        when(servicePaxPricingRepository.save(any(ServicePaxPricing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tourPaxRepository.save(any(TourPax.class))).thenAnswer(invocation -> {
            TourPax savedPax = invocation.getArgument(0);
            savedPax.setDeleted(true);
            return savedPax;
        });

        // Act
        GeneralResponse<String> response = tourPaxService.deleteTourPaxConfiguration(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().contains("marked as deleted"));

        // Verify repository calls
        verify(servicePaxPricingRepository, times(2)).save(any(ServicePaxPricing.class));
        verify(tourPaxRepository).save(any(TourPax.class));
        assertTrue(tourPax.getDeleted());
    }

    @Test
    void getTourPaxConfigurations_Success() {
        // Arrange
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        List<TourPax> paxList = Arrays.asList(tourPax);
        when(tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(1L)).thenReturn(paxList);
        when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);

        List<Long> tourDayIds = tourDays.stream().map(TourDay::getId).collect(Collectors.toList());
        when(tourDayServiceRepository.findByTourDayIdIn(tourDayIds)).thenReturn(tourDayServices);

        List<Long> paxIds = paxList.stream().map(TourPax::getId).collect(Collectors.toList());
        when(servicePaxPricingRepository.findByTourPaxIdInAndDeletedFalse(paxIds)).thenReturn(servicePaxPricings);

        // Act
        GeneralResponse<List<TourPaxFullDTO>> response = tourPaxService.getTourPaxConfigurations(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());

        TourPaxFullDTO paxDTO = response.getData().get(0);
        assertEquals(1L, paxDTO.getId());
        assertEquals(1L, paxDTO.getTourId());
        assertEquals(2, paxDTO.getMinPax());
        assertEquals(10, paxDTO.getMaxPax());
        assertEquals("2-10", paxDTO.getPaxRange());
        assertEquals(500.0, paxDTO.getFixedCost());
        assertEquals(100.0, paxDTO.getExtraHotelCost());
        assertEquals(200.0, paxDTO.getNettPricePerPax());
        assertEquals(300.0, paxDTO.getSellingPrice());
        assertTrue(paxDTO.isValid());
        assertNotNull(paxDTO.getServiceAssociations());
        assertEquals(2, paxDTO.getServiceAssociations().size());
        assertEquals(2, paxDTO.getServiceAssociationCount());
    }
}
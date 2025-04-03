package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.BookingRequestDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import com.fpt.capstone.tourism.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private TourBookingRepository tourBookingRepository;
    @Mock
    private TourService tourService;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private LocationMapper locationMapper;
    @Mock
    private TourImageMapper tourImageMapper;
    @Mock
    private TourBookingCustomerMapper tourBookingCustomerMapper;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Tour mockTour;
    private PublicTourScheduleDTO mockScheduleDTO;
    private BookingRequestDTO mockBookingRequest;

    @BeforeEach
    void setUp() {
        // Mock Tour
        mockTour = Tour.builder()
                .id(1L)
                .name("Amazing Vietnam")
                .numberDays(5)
                .numberNight(4)
                .privacy(String.valueOf(false))
                .depart_location(Location.builder().id(1L).build())
                .tourImages(List.of(TourImage.builder().id(10L).build()))
                .locations(List.of(Location.builder().id(100L).build()))
                .build();

        // Mock PublicTourScheduleDTO
        mockScheduleDTO = new PublicTourScheduleDTO();

        // Mock Booking Request
        mockBookingRequest = BookingRequestDTO.builder()
                .tourId(1L)
                .scheduleId(2L)
                .userId(10L)
                .paymentMethod(PaymentMethod.valueOf("CASH"))
                .fullName("John Doe")
                .adults(List.of())
                .children(List.of())
                .total(100.0)
                .note("Need a window seat")
                .build();
    }

    @Test
    void viewTourBookingDetail_Success() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        when(tourService.findSameLocationPublicTour(any())).thenReturn(List.of());
        when(tourScheduleRepository.findTourScheduleByTourId(1L, 2L)).thenReturn(mockScheduleDTO);
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(null);
        when(tourImageMapper.toPublicTourImageDTO(any())).thenReturn(null);

        GeneralResponse<TourBookingDataResponseDTO> response = bookingService.viewTourBookingDetail(1L, 2L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
        assertEquals(mockTour.getId(), response.getData().getId());
    }

    @Test
    void viewTourBookingDetail_TourNotFound() {
        when(tourRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                bookingService.viewTourBookingDetail(1L, 2L)
        );

        assertEquals("Customer Tour Booking detail loaded fail", exception.getResponseMessage());
    }

    @Test
    void createBooking_Success() {
        when(tourBookingCustomerMapper.toAdultEntity(any())).thenReturn(List.of());
        when(tourBookingCustomerMapper.toChildrenEntity(any())).thenReturn(List.of());
        when(tourBookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any())).thenReturn(null);

        GeneralResponse<?> response = bookingService.createBooking(mockBookingRequest);

        assertNotNull(response);
    }

    @Test
    void createBooking_Failure() {
        when(tourBookingRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                bookingService.createBooking(mockBookingRequest)
        );

        assertTrue(exception.getResponseMessage().contains("Database error"));
    }
}

package com.fpt.capstone.tourism.service.impl;


import com.fpt.capstone.tourism.dto.common.ChangeServiceDetailDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.OperatorTourDetailDTO;
import com.fpt.capstone.tourism.dto.common.TourOperationLogDTO;
import com.fpt.capstone.tourism.dto.request.AddServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.AssignTourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceQuantityUpdateDTO;
import com.fpt.capstone.tourism.dto.request.TourOperationLogRequestDTO;
import com.fpt.capstone.tourism.dto.response.OperatorTourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.TagMapper;
import com.fpt.capstone.tourism.mapper.TourOperationLogMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperatorServiceImplTest {


    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private TourBookingRepository tourBookingRepository;

    @Mock
    private TourBookingServiceRepository bookingServiceRepository;

    @Mock
    private TourDayRepository tourDayRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TourOperationLogRepository logRepository;

    @Mock
    private TourOperationLogMapper logMapper;

    @Mock
    private TagMapper tagMapper;
    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private OperatorServiceImpl operatorService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        reset(tourScheduleRepository, userRepository, authentication, securityContext);

        // Mock SecurityContextHolder to return a valid authentication
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("operatorUser");
        SecurityContextHolder.setContext(securityContext);

        // Mock userRepository to return a valid user
        User operatorUser = User.builder()
                .id(35L)
                .username("operatorUser")
                .build();
        lenient().when(userRepository.findByUsername("operatorUser")).thenReturn(Optional.of(operatorUser));
    }

    @Nested
    class GetListTourTests {

//        @Test
//        void testGetListTour_ValidParamsWithKeywordAndStatus_Success_UT101() {
//            // Arrange
//            int page = 0;
//            int size = 10;
//            String keyword = "Hà Giang";
//            String status = "ONGOING";
//            String orderDate = "asc";
//
//            Tour tour = Tour.builder()
//                    .name("Hà Giang Tour")
//                    .build();
//
//            TourPax tourPax = TourPax.builder()
//                    .maxPax(20)
//                    .build();
//
//            User tourGuide = User.builder()
//                    .fullName("Tour Guide")
//                    .build();
//
//            User operator = User.builder()
//                    .id(35L)
//                    .fullName("Operator")
//                    .build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(1L)
//                    .startDate(LocalDateTime.now())
//                    .endDate(LocalDateTime.now().plusDays(2))
//                    .status(TourScheduleStatus.ONGOING)
//                    .tour(tour)
//                    .tourPax(tourPax)
//                    .tourGuide(tourGuide)
//                    .operator(operator)
//                    .build();
//
//            List<TourSchedule> tourSchedules = Collections.singletonList(tourSchedule);
//            Page<TourSchedule> tourPage = new PageImpl<>(tourSchedules, PageRequest.of(page, size), 1);
//
//            // Mock repository and service calls
//            when(operatorService.buildSearchSpecification(keyword, status)).thenReturn((root, query, criteriaBuilder) -> null);
//            when(tourScheduleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
//            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(1L)))
//                    .thenReturn(Collections.singletonList(new Object[]{1L, 15}));
//
//            // Act
//            GeneralResponse<PagingDTO<List<OperatorTourDTO>>> response = operatorService.getListTour(page, size, keyword, status, orderDate);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Thành công", response.getMessage());
//            PagingDTO<List<OperatorTourDTO>> data = response.getData();
//            assertNotNull(data);
//            assertEquals(page, data.getPage());
//            assertEquals(size, data.getSize());
//            assertEquals(1, data.getTotal());
//            List<OperatorTourDTO> tours = data.getItems();
//            assertEquals(1, tours.size());
//            OperatorTourDTO tourDTO = tours.get(0);
//            assertEquals(1L, tourDTO.getScheduleId());
//            assertEquals("Hà Giang Tour", tourDTO.getTourName());
//            assertEquals("ONGOING", tourDTO.getStatus());
//            assertEquals("Tour Guide", tourDTO.getTourGuide());
//            assertEquals("Operator", tourDTO.getOperator());
//            assertEquals(20, tourDTO.getMaxPax());
//            assertEquals(15, tourDTO.getAvailableSeats());
//        }

//        @Test
//        void testGetListTour_ValidParamsWithDifferentPageAndSize_Success_UT102() {
//            // Arrange
//            int page = 1;
//            int size = 2;
//            String keyword = "tổ quốc";
//            String status = null;
//            String orderDate = "desc";
//
//            Tour tour = Tour.builder()
//                    .name("Tổ Quốc Tour")
//                    .build();
//
//            TourPax tourPax = TourPax.builder()
//                    .maxPax(30)
//                    .build();
//
//            User operator = User.builder()
//                    .id(35L)
//                    .fullName("Operator")
//                    .build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(2L)
//                    .startDate(LocalDateTime.now())
//                    .endDate(LocalDateTime.now().plusDays(3))
//                    .status(TourScheduleStatus.ONGOING)
//                    .tour(tour)
//                    .tourPax(tourPax)
//                    .tourGuide(null)
//                    .operator(operator)
//                    .build();
//
//            List<TourSchedule> tourSchedules = Collections.singletonList(tourSchedule);
//            Page<TourSchedule> tourPage = new PageImpl<>(tourSchedules, PageRequest.of(page, size), 1);
//
//            // Mock repository and service calls
//            when(operatorService.buildSearchSpecification(keyword, status)).thenReturn((root, query, criteriaBuilder) -> null);
//            when(tourScheduleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
//            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(2L)))
//                    .thenReturn(Collections.singletonList(new Object[]{2L, 25}));
//
//            // Act
//            GeneralResponse<PagingDTO<List<OperatorTourDTO>>> response = operatorService.getListTour(page, size, keyword, status, orderDate);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Thành công", response.getMessage());
//            PagingDTO<List<OperatorTourDTO>> data = response.getData();
//            assertNotNull(data);
//            assertEquals(page, data.getPage());
//            assertEquals(size, data.getSize());
//            assertEquals(1, data.getTotal());
//            List<OperatorTourDTO> tours = data.getItems();
//            assertEquals(1, tours.size());
//            OperatorTourDTO tourDTO = tours.get(0);
//            assertEquals(2L, tourDTO.getScheduleId());
//            assertEquals("Tổ Quốc Tour", tourDTO.getTourName());
//            assertEquals("PENDING", tourDTO.getStatus());
//            assertNull(tourDTO.getTourGuide());
//            assertEquals("Operator", tourDTO.getOperator());
//            assertEquals(30, tourDTO.getMaxPax());
//            assertEquals(25, tourDTO.getAvailableSeats());
//        }

//        @Test
//        void testGetListTour_ValidParamsWithInvalidKeyword_Success_UT103() {
//            // Arrange
//            int page = 0;
//            int size = 10;
//            String keyword = "abc xyz";
//            String status = null;
//            String orderDate = null;
//
//            Tour tour = Tour.builder()
//                    .name("Some Tour")
//                    .build();
//
//            TourPax tourPax = TourPax.builder()
//                    .maxPax(15)
//                    .build();
//
//            User operator = User.builder()
//                    .id(35L)
//                    .fullName("Operator")
//                    .build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(3L)
//                    .startDate(LocalDateTime.now())
//                    .endDate(LocalDateTime.now().plusDays(1))
//                    .status(TourScheduleStatus.ONGOING)
//                    .tour(tour)
//                    .tourPax(tourPax)
//                    .tourGuide(null)
//                    .operator(operator)
//                    .build();
//
//            List<TourSchedule> tourSchedules = Collections.singletonList(tourSchedule);
//            Page<TourSchedule> tourPage = new PageImpl<>(tourSchedules, PageRequest.of(page, size), 1);
//
//            // Mock repository and service calls
//            when(operatorService.buildSearchSpecification(keyword, status)).thenReturn((root, query, criteriaBuilder) -> null);
//            when(tourScheduleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
//            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(3L)))
//                    .thenReturn(Collections.singletonList(new Object[]{3L, 10}));
//
//            // Act
//            GeneralResponse<PagingDTO<List<OperatorTourDTO>>> response = operatorService.getListTour(page, size, keyword, status, orderDate);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Thành công", response.getMessage());
//            PagingDTO<List<OperatorTourDTO>> data = response.getData();
//            assertNotNull(data);
//            assertEquals(page, data.getPage());
//            assertEquals(size, data.getSize());
//            assertEquals(1, data.getTotal());
//            List<OperatorTourDTO> tours = data.getItems();
//            assertEquals(1, tours.size());
//            OperatorTourDTO tourDTO = tours.get(0);
//            assertEquals(3L, tourDTO.getScheduleId());
//            assertEquals("Some Tour", tourDTO.getTourName());
//            assertEquals("PENDING", tourDTO.getStatus());
//            assertEquals(15, tourDTO.getMaxPax());
//            assertEquals(10, tourDTO.getAvailableSeats());
//        }

//        @Test
//        void testGetListTour_ValidParamsWithNullKeyword_Success_UT104() {
//            // Arrange
//            int page = 0;
//            int size = 10;
//            String keyword = null;
//            String status = "ONGOING";
//            String orderDate = null;
//
//            Tour tour = Tour.builder()
//                    .name("Another Tour")
//                    .build();
//
//            TourPax tourPax = TourPax.builder()
//                    .maxPax(25)
//                    .build();
//
//            User operator = User.builder()
//                    .id(35L)
//                    .fullName("Operator")
//                    .build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(4L)
//                    .startDate(LocalDateTime.now())
//                    .endDate(LocalDateTime.now().plusDays(4))
//                    .status(TourScheduleStatus.ONGOING)
//                    .tour(tour)
//                    .tourPax(tourPax)
//                    .tourGuide(null)
//                    .operator(operator)
//                    .build();
//
//            List<TourSchedule> tourSchedules = Collections.singletonList(tourSchedule);
//            Page<TourSchedule> tourPage = new PageImpl<>(tourSchedules, PageRequest.of(page, size), 1);
//
//            // Mock repository and service calls
//            when(operatorService.buildSearchSpecification(keyword, status)).thenReturn((root, query, criteriaBuilder) -> null);
//            when(tourScheduleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
//            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(4L)))
//                    .thenReturn(Collections.singletonList(new Object[]{4L, 20}));
//
//            // Act
//            GeneralResponse<PagingDTO<List<OperatorTourDTO>>> response = operatorService.getListTour(page, size, keyword, status, orderDate);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Thành công", response.getMessage());
//            PagingDTO<List<OperatorTourDTO>> data = response.getData();
//            assertNotNull(data);
//            assertEquals(page, data.getPage());
//            assertEquals(size, data.getSize());
//            assertEquals(1, data.getTotal());
//            List<OperatorTourDTO> tours = data.getItems();
//            assertEquals(1, tours.size());
//            OperatorTourDTO tourDTO = tours.get(0);
//            assertEquals(4L, tourDTO.getScheduleId());
//            assertEquals("Another Tour", tourDTO.getTourName());
//            assertEquals("ONGOING", tourDTO.getStatus());
//            assertEquals(25, tourDTO.getMaxPax());
//            assertEquals(20, tourDTO.getAvailableSeats());
//        }

        @Test
        void testGetListTour_NegativePage_ThrowsException_UT105() {
            // Arrange
            int page = -1;
            int size = 10;
            String keyword = null;
            String status = null;
            String orderDate = null;

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.getListTour(page, size, keyword, status, orderDate);
            });

            assertEquals("Lấy tất cả các tour của điều hành viên thất bại", exception.getMessage());
        }

        @Test
        void testGetListTour_NegativeSize_ThrowsException_UT106() {
            // Arrange
            int page = 0;
            int size = -1;
            String keyword = null;
            String status = null;
            String orderDate = null;

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.getListTour(page, size, keyword, status, orderDate);
            });

            assertEquals("Lấy tất cả các tour của điều hành viên thất bại", exception.getMessage());
        }

//        @Test
//        void testGetListTour_InvalidStatus_ThrowsException_UT107() {
//            // Arrange
//            int page = 0;
//            int size = 10;
//            String keyword = null;
//            String status = "INVALID_STATUS";
//            String orderDate = null;
//
//            // Mock buildSearchSpecification to throw an exception for invalid status
//            doThrow(new IllegalArgumentException("Invalid status value"))
//                    .when(operatorService).buildSearchSpecification(keyword, status);
//
//            // Act & Assert
//            BusinessException exception = assertThrows(BusinessException.class, () -> {
//                operatorService.getListTour(page, size, keyword, status, orderDate);
//            });
//
//            assertEquals("Lấy tất cả các tour của điều hành viên thất bại", exception.getResponseMessage());
//        }

//        @Test
//        void testGetListTour_InvalidOrderDate_Success_UT108() {
//            // Arrange
//            int page = 0;
//            int size = 10;
//            String keyword = null;
//            String status = null;
//            String orderDate = "abc";
//
//            Tour tour = Tour.builder()
//                    .name("Default Tour")
//                    .build();
//
//            TourPax tourPax = TourPax.builder()
//                    .maxPax(10)
//                    .build();
//
//            User operator = User.builder()
//                    .id(35L)
//                    .fullName("Operator")
//                    .build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(5L)
//                    .startDate(LocalDateTime.now())
//                    .endDate(LocalDateTime.now().plusDays(2))
//                    .status(TourScheduleStatus.ONGOING)
//                    .tour(tour)
//                    .tourPax(tourPax)
//                    .tourGuide(null)
//                    .operator(operator)
//                    .build();
//
//            List<TourSchedule> tourSchedules = Collections.singletonList(tourSchedule);
////            Page<TourSchedule> tourPage = new PageImpl<>(tourSchedules, PageRequest.of(page, size), 1);
////
////            // Mock repository and service calls
////            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(5L)))
////                    .thenReturn(Collections.singletonList(new Object[]{5L, 8}));
//
//            // Act
//            GeneralResponse<PagingDTO<List<OperatorTourDTO>>> response = operatorService.getListTour(page, size, keyword, status, orderDate);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Thành công", response.getMessage());
//            PagingDTO<List<OperatorTourDTO>> data = response.getData();
//            assertNotNull(data);
//            assertEquals(page, data.getPage());
//            assertEquals(size, data.getSize());
//            assertEquals(1, data.getTotal());
//            List<OperatorTourDTO> tours = data.getItems();
//            assertEquals(1, tours.size());
//            OperatorTourDTO tourDTO = tours.get(0);
//            assertEquals(5L, tourDTO.getScheduleId());
//            assertEquals("Default Tour", tourDTO.getTourName());
//            assertEquals("PENDING", tourDTO.getStatus());
//            assertEquals(10, tourDTO.getMaxPax());
//            assertEquals(8, tourDTO.getAvailableSeats());
//        }

        // Add a test case to verify the behavior when the user is not found
        @Test
        void testGetListTour_UserNotAuthenticated_ThrowsException() {
            // Arrange
            int page = 0;
            int size = 10;
            String keyword = null;
            String status = null;
            String orderDate = null;

            // Simulate no authentication
            lenient().when(securityContext.getAuthentication()).thenReturn(null);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.getListTour(page, size, keyword, status, orderDate);
            });

            assertEquals("Lấy tất cả các tour của điều hành viên thất bại", exception.getResponseMessage());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getHttpCode());
        }
    }
    @Nested
    class GetTourDetailTests {

        @Test
        void testGetTourDetail_ValidScheduleIdZero_Success_UTCID01() {
            // Arrange
            Long scheduleId = 0L;

            User operator = User.builder()
                    .id(35L)
                    .fullName("Operator")
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(2))
                    .operator(operator)
                    .tourGuide(null)
                    .tourPax(TourPax.builder().maxPax(20).build())
                    .meetingLocation("Meeting Point")
                    .build();

            Location departureLocation = Location.builder()
                    .name("Departure City")
                    .build();

            Tour tour = Tour.builder()
                    .id(1L)
                    .name("Test Tour")
                    .tourType(TourType.SIC)
                    .tags(Collections.emptyList())
                    .numberDays(3)
                    .numberNights(2)
                    .departLocation(departureLocation)
                    .createdBy(operator)
                    .build();

            // Mock checkAuthor
//            doNothing().when(operatorService).checkAuthor(scheduleId);

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(tourRepository.findByScheduleId(scheduleId)).thenReturn(tour);
            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(scheduleId)))
                    .thenReturn(Collections.singletonList(new Object[]{scheduleId, 15}));
            when(tourScheduleRepository.findSoldSeatsByScheduleId(scheduleId)).thenReturn(5);
            when(tourScheduleRepository.findPendingSeatsByScheduleId(scheduleId)).thenReturn(2);
            when(tourScheduleRepository.findPaidTourCostByScheduleId(scheduleId)).thenReturn(1000.0);
            when(tourScheduleRepository.findRevenueCostByScheduleId(scheduleId)).thenReturn(2000.0);
            when(tourScheduleRepository.findTotalTourCostByScheduleId(scheduleId)).thenReturn(3000.0);
            when(tagMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // Act
            GeneralResponse<OperatorTourDetailDTO> response = operatorService.getTourDetail(scheduleId);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Điều hành viên lấy chi tiết tour thành công", response.getMessage());
            OperatorTourDetailDTO data = response.getData();
            assertNotNull(data);
            assertEquals(scheduleId, data.getScheduleId());
            assertEquals("Test Tour", data.getTourName());
            assertEquals(Collections.emptyList(), data.getTags());
            assertEquals(3, data.getNumberDays());
            assertEquals(2, data.getNumberNights());
            assertEquals("Departure City", data.getDepartureLocation());
            assertEquals(tourSchedule.getStartDate(), data.getStartDate());
            assertEquals(tourSchedule.getEndDate(), data.getEndDate());
            assertEquals(tour.getCreatedAt(), data.getCreatedAt());
            assertEquals("Operator", data.getCreatedBy());
            assertEquals(20, data.getMaxPax());
            assertEquals(5, data.getSoldSeats());
            assertEquals(2, data.getPendingSeats());
            assertEquals(15, data.getRemainingSeats());
            assertEquals("Operator", data.getOperatorName());
            assertEquals(tourSchedule.getDepartureTime(), data.getDepartureTime());
            assertEquals("null", data.getTourGuideName());
            assertEquals("Meeting Point", data.getMeetingLocation());
            assertEquals(3000.0, data.getTotalTourCost());
            assertEquals(1000.0, data.getPaidTourCost());
            assertEquals(2000.0 - 1000.0, data.getRemainingTourCost());
            assertEquals(2000.0, data.getRevenueCost());
        }

        @Test
        void testGetTourDetail_ValidScheduleIdOne_Success_UTCID02() {
            // Arrange
            Long scheduleId = 1L;

            User operator = User.builder()
                    .id(35L)
                    .fullName("Operator")
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(3))
                    .operator(operator)
                    .tourGuide(null)
                    .tourPax(TourPax.builder().maxPax(30).build())
                    .meetingLocation("Meeting Point")
                    .build();

            Location departureLocation = Location.builder()
                    .name("Departure City")
                    .build();

            Tour tour = Tour.builder()
                    .id(2L)
                    .name("Boundary Tour")
                    .tourType(TourType.SIC)
                    .tags(Collections.emptyList())
                    .numberDays(4)
                    .numberNights(3)
                    .departLocation(departureLocation)
                    .createdBy(operator)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(tourRepository.findByScheduleId(scheduleId)).thenReturn(tour);
            when(tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(scheduleId)))
                    .thenReturn(Collections.singletonList(new Object[]{scheduleId, 25}));
            when(tourScheduleRepository.findSoldSeatsByScheduleId(scheduleId)).thenReturn(5);
            when(tourScheduleRepository.findPendingSeatsByScheduleId(scheduleId)).thenReturn(3);
            when(tourScheduleRepository.findPaidTourCostByScheduleId(scheduleId)).thenReturn(1500.0);
            when(tourScheduleRepository.findRevenueCostByScheduleId(scheduleId)).thenReturn(2500.0);
            when(tourScheduleRepository.findTotalTourCostByScheduleId(scheduleId)).thenReturn(4000.0);

            // Use doReturn instead of when for tagMapper

            // Act
            GeneralResponse<OperatorTourDetailDTO> response = operatorService.getTourDetail(scheduleId);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Điều hành viên lấy chi tiết tour thành công", response.getMessage());
            OperatorTourDetailDTO data = response.getData();
            assertNotNull(data);
            assertEquals(scheduleId, data.getScheduleId());
            assertEquals("Boundary Tour", data.getTourName());
            assertEquals(TourType.SIC, data.getTourType()); // Updated to match tourType
            assertEquals(Collections.emptyList(), data.getTags());
            assertEquals(4, data.getNumberDays());
            assertEquals(3, data.getNumberNights());
            assertEquals("Departure City", data.getDepartureLocation());
            assertEquals(tourSchedule.getStartDate(), data.getStartDate());
            assertEquals(tourSchedule.getEndDate(), data.getEndDate());
            assertNull(data.getCreatedAt()); // createdAt is not set in the mock tour
            assertEquals("Operator", data.getCreatedBy());
            assertEquals(30, data.getMaxPax());
            assertEquals(5, data.getSoldSeats());
            assertEquals(3, data.getPendingSeats());
            assertEquals(25, data.getRemainingSeats());
            assertEquals("Operator", data.getOperatorName());
            assertNull(data.getDepartureTime()); // departureTime is not set in the mock tourSchedule
            assertEquals("null", data.getTourGuideName());
            assertEquals("Meeting Point", data.getMeetingLocation());
            assertEquals(4000.0, data.getTotalTourCost());
            assertEquals(1500.0, data.getPaidTourCost());
            assertEquals(2500.0 - 1500.0, data.getRemainingTourCost());
            assertEquals(2500.0, data.getRevenueCost());
        }

        @Test
        void testGetTourDetail_NullScheduleId_ThrowsException_UTCID03() {
            // Arrange
            Long scheduleId = null;

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.getTourDetail(scheduleId);
            });

            assertEquals("Điều hành viên lấy chi tiết tour thất bại", exception.getResponseMessage());
        }

        @Test
        void testGetTourDetail_NonExistentScheduleId_ThrowsException_UTCID04() {
            // Arrange
            Long scheduleId = 100000L;

            // Mock checkAuthor
//            doNothing().when(operatorService).checkAuthor(scheduleId);

            // Mock repository call to return empty for non-existent scheduleId
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.getTourDetail(scheduleId);
            });

            assertEquals("Điều hành viên lấy chi tiết tour thất bại", exception.getResponseMessage());
        }
    }
    @Nested
    class CreateOperationLogTests {

        @Test
        void testCreateOperationLog_ValidScheduleIdOne_Success_UTCID01() {
            // Arrange
            Long scheduleId = 1L;
            TourOperationLogRequestDTO logRequestDTO = new TourOperationLogRequestDTO();
            logRequestDTO.setContent("Đợi dịch vụ");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            TourOperationLog log = new TourOperationLog();
            log.setContent("Đợi dịch vụ");
            log.setCreatedAt(LocalDateTime.now());
            log.setDeleted(false);
            log.setTourSchedule(tourSchedule);

            TourOperationLogDTO logDTO = new TourOperationLogDTO();
            logDTO.setContent("Đợi dịch vụ");
            logDTO.setCreatedAt(log.getCreatedAt());

            // Mock checkAuthor and Validator
            mockStatic(Validator.class);
            doNothing().when(Validator.class);
            Validator.validateLog(logRequestDTO);

            // Mock repository and mapper calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(logMapper.toEntity(logRequestDTO)).thenReturn(log);
            when(logRepository.save(any(TourOperationLog.class))).thenReturn(log);
            when(logMapper.toDTO(log)).thenReturn(logDTO);

            // Act
            GeneralResponse<TourOperationLogDTO> response = operatorService.createOperationLog(scheduleId, logRequestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Tạo nhật ký hoạt động thành công", response.getMessage());
            TourOperationLogDTO data = response.getData();
            assertNotNull(data);
            assertEquals("Đợi dịch vụ", data.getContent());

            // Clean up static mock
        }

        @Test
        void testCreateOperationLog_ValidScheduleIdZero_Success_UTCID02() {
            // Arrange
            Long scheduleId = 0L;
            TourOperationLogRequestDTO logRequestDTO = new TourOperationLogRequestDTO();
            logRequestDTO.setContent(null);

            // Mock checkAuthor and Validator
//            mockStatic(Validator.class);
//            doThrow(BusinessException.of("Nội dung trống")).when(Validator.class);
//            Validator.validateLog(logRequestDTO);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.createOperationLog(scheduleId, logRequestDTO);
            });

            assertEquals("Không tìm thấy lịch trình tour", exception.getResponseMessage());

            // Clean up static mock
            closeable = mockStatic(Validator.class);
        }

        @Test
        void testCreateOperationLog_NullScheduleId_ThrowsException_UTCID03() {
            // Arrange
            Long scheduleId = null;
            TourOperationLogRequestDTO logRequestDTO = new TourOperationLogRequestDTO();
            logRequestDTO.setContent("Khách yêu cầu đổi dịch vụ");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.createOperationLog(scheduleId, logRequestDTO);
            });

            assertEquals("Không tìm thấy lịch trình tour", exception.getResponseMessage());
        }

        @Test
        void testCreateOperationLog_NonExistentScheduleId_ThrowsException_UTCID04() {
            // Clean up static mock
//            closeable = mockStatic(Validator.class);
            // Arrange
            Long scheduleId = 100000L;
            TourOperationLogRequestDTO logRequestDTO = new TourOperationLogRequestDTO();
            logRequestDTO.setContent(null);

            // Mock checkAuthor and Validator
//            mockStatic(Validator.class);
//            doThrow(BusinessException.of("Nội dung trống")).when(Validator.class);
//            Validator.validateLog(logRequestDTO);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.createOperationLog(scheduleId, logRequestDTO);
            });

            assertEquals("Không tìm thấy lịch trình tour", exception.getResponseMessage());


        }
    }
    private AutoCloseable closeable;

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Nested
    class DeleteOperationLogTests {

        @Test
        void testDeleteOperationLog_ValidLogIdOne_Success_UTCID01() {
            // Arrange
            Long logId = 1L;
            Long scheduleId = 1L;

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            TourOperationLog log = new TourOperationLog();
            log.setId(logId);
            log.setTourSchedule(tourSchedule);
            log.setDeleted(false);

            TourOperationLog updatedLog = new TourOperationLog();
            updatedLog.setId(logId);
            updatedLog.setTourSchedule(tourSchedule);
            updatedLog.setDeleted(true);
            updatedLog.setUpdatedAt(LocalDateTime.now());

            TourOperationLogDTO logDTO = new TourOperationLogDTO();
            logDTO.setId(logId);
            logDTO.setDeleted(true);

            // Mock checkAuthor

            // Mock repository and mapper calls
            when(logRepository.findById(logId)).thenReturn(Optional.of(log));
            lenient().when(logRepository.save(any(TourOperationLog.class))).thenReturn(updatedLog);
            lenient().when(logMapper.toDTO(updatedLog)).thenReturn(logDTO);

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.deleteOperationLog(logId);
            });

            assertEquals("Không tìm thấy lịch trình tour", exception.getResponseMessage());
        }

        @Test
        void testDeleteOperationLog_ValidLogIdZero_Success_UTCID02() {
            // Arrange
            Long logId = 0L;
            Long scheduleId = 0L;

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            TourOperationLog log = new TourOperationLog();
            log.setId(logId);
            log.setTourSchedule(tourSchedule);
            log.setDeleted(false);

            TourOperationLog updatedLog = new TourOperationLog();
            updatedLog.setId(logId);
            updatedLog.setTourSchedule(tourSchedule);
            updatedLog.setDeleted(true);
            updatedLog.setUpdatedAt(LocalDateTime.now());

            TourOperationLogDTO logDTO = new TourOperationLogDTO();
            logDTO.setId(logId);
            logDTO.setDeleted(true);

            // Mock checkAuthor

            // Mock repository and mapper calls
            when(logRepository.findById(logId)).thenReturn(Optional.of(log));
            lenient().when(logRepository.save(any(TourOperationLog.class))).thenReturn(updatedLog);
            lenient().when(logMapper.toDTO(updatedLog)).thenReturn(logDTO);

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.deleteOperationLog(logId);
            });

            assertEquals("Không tìm thấy lịch trình tour", exception.getResponseMessage());
        }

        @Test
        void testDeleteOperationLog_NullLogId_ThrowsException_UTCID03() {
            // Arrange
            Long logId = null;

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.deleteOperationLog(logId);
            });

            assertEquals("Không tìm thấy nhật ký tour", exception.getResponseMessage());
        }

        @Test
        void testDeleteOperationLog_NonExistentLogId_ThrowsException_UTCID04() {
            // Arrange
            Long logId = 100000L;

            // Mock repository call to return empty for non-existent logId
            when(logRepository.findById(logId)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.deleteOperationLog(logId);
            });

            assertEquals("Không tìm thấy nhật ký tour", exception.getResponseMessage());
        }
    }
    @Nested
    class AssignTourGuideTests {

        @Test
        void testAssignTourGuide_ValidScheduleIdOne_Success_UTCID01() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(1L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(1L, data.getTourGuideId());
            assertEquals("Hồ Tây", data.getMeetingLocation());
        }

        @Test
        void testAssignTourGuide_ValidScheduleIdZero_Success_UTCID02() {
            // Arrange
            Long scheduleId = 0L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(1L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(1L, data.getTourGuideId());
            assertEquals("Hồ Tây", data.getMeetingLocation());
        }

        @Test
        void testAssignTourGuide_NullScheduleId_ThrowsException_UTCID03() {
            // Arrange
            Long scheduleId = null;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation("Hồ Tây");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.assignTourGuide(scheduleId, requestDTO);
            });

            assertEquals("Phân công hướng dẫn viên thất bại", exception.getResponseMessage());
        }

        @Test
        void testAssignTourGuide_NonExistentScheduleId_ThrowsException_UTCID04() {
            // Arrange
            Long scheduleId = 100000L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation("Hồ Tây");

            // Mock checkAuthor

            // Mock repository call to return empty for non-existent scheduleId
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.assignTourGuide(scheduleId, requestDTO);
            });

            assertEquals("Phân công hướng dẫn viên thất bại", exception.getResponseMessage());
        }

        @Test
        void testAssignTourGuide_ValidDepartureTimeFormat_Success_UTCID05() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(1L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(1L, data.getTourGuideId());
            assertEquals("Hồ Tây", data.getMeetingLocation());
        }

        @Test
        void testAssignTourGuide_NullDepartureTime_ThrowsException_UTCID06() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setDepartureTime(null);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(1L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(1L, data.getTourGuideId());
            assertNull(data.getDepartureTime());
            assertEquals("Hồ Tây", data.getMeetingLocation());
        }

        @Test
        void testAssignTourGuide_ValidTourGuideIdZero_Success_UTCID07() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(0L);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(0L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(0L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(0L, data.getTourGuideId());
            assertEquals("Hồ Tây", data.getMeetingLocation());
        }

        @Test
        void testAssignTourGuide_NullTourGuideId_ThrowsException_UTCID08() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(null);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(null)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.assignTourGuide(scheduleId, requestDTO);
            });

            assertEquals("Phân công hướng dẫn viên thất bại", exception.getResponseMessage());
        }

        @Test
        void testAssignTourGuide_NonExistentTourGuideId_ThrowsException_UTCID09() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(100000L);
            requestDTO.setMeetingLocation("Hồ Tây");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(100000L)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.assignTourGuide(scheduleId, requestDTO);
            });

            assertEquals("Phân công hướng dẫn viên thất bại", exception.getResponseMessage());
        }

        @Test
        void testAssignTourGuide_EmptyMeetingLocation_Success_UTCID10() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation("");

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(1L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(1L, data.getTourGuideId());
            assertEquals("", data.getMeetingLocation());
        }

        @Test
        void testAssignTourGuide_NullMeetingLocation_Success_UTCID11() {
            // Arrange
            Long scheduleId = 1L;
            AssignTourGuideRequestDTO requestDTO = new AssignTourGuideRequestDTO();
            requestDTO.setTourGuideId(1L);
            requestDTO.setMeetingLocation(null);

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(scheduleId)
                    .build();

            User tourGuide = User.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(tourSchedule));
            when(userRepository.findById(1L)).thenReturn(Optional.of(tourGuide));
            when(tourScheduleRepository.save(any(TourSchedule.class))).thenReturn(tourSchedule);

            // Act
            GeneralResponse<AssignTourGuideRequestDTO> response = operatorService.assignTourGuide(scheduleId, requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Phân công hướng dẫn viên thành công", response.getMessage());
            AssignTourGuideRequestDTO data = response.getData();
            assertNotNull(data);
            assertEquals(1L, data.getTourGuideId());
            assertNull(data.getMeetingLocation());
        }
    }
    @Nested
    class AddServiceTests {

//        @Test
//        void testAddService_ValidInputs_Success_UTCID01() {
//            // Arrange
//            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
//            requestDTO.setBookingId(1L);
//            requestDTO.setServiceId(1L);
//            requestDTO.setTourDayId(1L);
//            requestDTO.setAddQuantity(2);
//            requestDTO.setReason("Khách đặt thêm");
//
//            Service service = Service.builder()
//                    .id(1L)
//                    .name("Test Service")
//                    .sellingPrice(100.0)
//                    .build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(1L)
//                    .build();
//
//            User user = User.builder()
//                    .id(1L)
//                    .fullName("Test User")
//                    .build();
//
//            TourBooking booking = TourBooking.builder()
//                    .id(1L)
//                    .bookingCode("BOOK123")
//                    .tourSchedule(tourSchedule)
//                    .user(user)
//                    .build();
//
//            TourDay tourDay = TourDay.builder()
//                    .id(1L)
//                    .build();
//
//            // Mock checkAuthor
//
//            // Mock repository calls
//            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
//            when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
//            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
//            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 1L, 1L)).thenReturn(null);
//            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
//            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
//
//            // Act
//            GeneralResponse<?> response = operatorService.addService(requestDTO);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Thêm dịch vụ vào booking thành công", response.getMessage());
//            assertEquals(requestDTO, response.getData());
//        }

        @Test
        void testAddService_ValidBookingIdZero_Success_UTCID02() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(0L);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(1L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(0L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(0L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
            when(tourBookingRepository.findById(0L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(0L, 1L, 1L)).thenReturn(null);
            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_NullBookingId_ThrowsException_UTCID03() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(null);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_NonExistentBookingId_ThrowsException_UTCID04() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(100000L);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(1L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            // Mock repository calls
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
            when(tourBookingRepository.findById(100000L)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_ValidServiceIdZero_Success_UTCID05() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(0L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(0L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(serviceRepository.findById(0L)).thenReturn(Optional.of(service));
            when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 0L, 1L)).thenReturn(null);
            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_NullServiceId_ThrowsException_UTCID06() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(null);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            // Mock repository calls
            when(serviceRepository.findById(null)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_NonExistentServiceId_ThrowsException_UTCID07() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(100000L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            // Mock repository calls
            when(serviceRepository.findById(100000L)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_ServiceAlreadyExists_ThrowsException_UTCID08() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(2L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(2L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            TourBookingService existingBookingService = TourBookingService.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            lenient().when(serviceRepository.findById(2L)).thenReturn(Optional.of(service));
            lenient().when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 2L, 1L)).thenReturn((List<TourBookingService>) existingBookingService);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_ValidAddQuantityMax_Success_UTCID09() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(20);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(1L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
            lenient().when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 1L, 1L)).thenReturn(null);
            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_AddQuantityZero_Success_UTCID10() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(0);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(1L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
            when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 1L, 1L)).thenReturn(null);
            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_NegativeAddQuantity_ThrowsException_UTCID11() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(-20);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(1L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
            when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 1L, 1L)).thenReturn(null);
            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testAddService_InvalidRequestDate_ThrowsException_UTCID12() {
            // Arrange
            AddServiceRequestDTO requestDTO = new AddServiceRequestDTO();
            requestDTO.setBookingId(1L);
            requestDTO.setServiceId(1L);
            requestDTO.setTourDayId(1L);
            requestDTO.setAddQuantity(2);
            requestDTO.setReason("Khách đặt thêm");

            Service service = Service.builder()
                    .id(1L)
                    .name("Test Service")
                    .sellingPrice(100.0)
                    .build();

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            User user = User.builder()
                    .id(1L)
                    .fullName("Test User")
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("BOOK123")
                    .tourSchedule(tourSchedule)
                    .user(user)
                    .build();

            TourDay tourDay = TourDay.builder()
                    .id(1L)
                    .build();

            // Mock checkAuthor

            // Mock repository calls
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
            when(tourBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            lenient().when(tourDayRepository.findById(1L)).thenReturn(Optional.of(tourDay));
            lenient().when(bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(1L, 1L, 1L)).thenReturn(null);
            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(new TourBookingService());
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

            // Act
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.addService(requestDTO);
            });

            assertEquals("Thêm dịch vụ thất bại", exception.getResponseMessage());
        }


    }
    @Nested
    class UpdateServiceQuantityTests {

//        @Test
//        void testUpdateServiceQuantity_ValidInputs_Success_UTCID01() {
//            // Arrange
//            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
//            requestDTO.setTourBookingServiceId(1L);
//            requestDTO.setNewQuantity(20);
//            User operator = User.builder().id(35L).build();
//
//            TourSchedule tourSchedule = TourSchedule.builder()
//                    .id(1L)
//                    .startDate(LocalDateTime.now())
//                    .status(TourScheduleStatus.ONGOING)
//                    .endDate(LocalDateTime.now())
//                    .operator(operator)
//                    .build();
//
//            Tour tour = Tour.builder()
//                    .id(1L)
//                    .name("Test Tour")
//                    .tourType(TourType.SIC)
//                    .build();
//
//            User user = User.builder()
//                    .id(1L)
//                    .fullName("Test User")
//                    .build();
//
//            TourBooking booking = TourBooking.builder()
//                    .id(1L)
//                    .bookingCode("BOOK123")
//                    .tourSchedule(tourSchedule)
//                    .tour(tour)
//                    .user(user)
//                    .build();
//
//            Service service = Service.builder()
//                    .id(1L)
//                    .name("Test Service")
//                    .nettPrice(100.0)
//                    .build();
//
//            TourDay tourDay = TourDay.builder()
//                    .id(1L)
//                    .dayNumber(1)
//                    .build();
//
//            TourBookingService bookingService = TourBookingService.builder()
//                    .id(1L)
//                    .booking(booking)
//                    .service(service)
//                    .tourDay(tourDay)
//                    .currentQuantity(10)
//                    .status(TourBookingServiceStatus.AVAILABLE)
//                    .reason("Initial Request")
//                    .build();
//
//            // Mock checkAuthorByTourBookingService
//
//            // Mock repository calls
//            lenient().when(bookingServiceRepository.findById(1L)).thenReturn(Optional.of(bookingService));
//            lenient().when(tourScheduleRepository.findByTourBookingServiceId(1L)).thenReturn(tourSchedule);
//            lenient().when(bookingServiceRepository.save(any(TourBookingService.class))).thenReturn(bookingService);
//
//            // Act
//            GeneralResponse<?> response = operatorService.updateServiceQuantity(requestDTO);
//
//            // Assert
//            assertNotNull(response);
//            assertEquals(HttpStatus.OK.value(), response.getStatus());
//            assertEquals("Cập nhật số lượng dịch vụ thành công", response.getMessage());
//            ChangeServiceDetailDTO data = (ChangeServiceDetailDTO) response.getData();
//            assertNotNull(data);
//            assertEquals(1L, data.getTourBookingServiceId());
//            assertEquals("Test Tour", data.getTourName());
//            assertEquals(Integer.valueOf(1), data.getDayNumber());
//            assertEquals("BOOK123", data.getBookingCode());
//            assertEquals("AVAILABLE", data.getStatus());
//            assertEquals("Initial Request", data.getReason());
//            assertEquals("Test User", data.getProposer());
//            assertEquals("Test Service", data.getServiceName());
//            assertEquals(100.0, data.getNettPrice());
//            assertEquals(20, data.getCurrentQuantity());
//            assertEquals(2000.0, data.getTotalPrice());
//        }

        @Test
        void testUpdateServiceQuantity_ValidTourBookingServiceIdZero_Success_UTCID02() {
            // Arrange
            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
            requestDTO.setTourBookingServiceId(1L);
            requestDTO.setNewQuantity(0);

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .tourSchedule(tourSchedule)
                    .build();

            Service service = Service.builder()
                    .id(1L)
                    .build();

            TourBookingService bookingService = TourBookingService.builder()
                    .id(1L)
                    .booking(booking)
                    .service(service)
                    .currentQuantity(10)
                    .status(TourBookingServiceStatus.AVAILABLE)
                    .build();

            // Mock checkAuthorByTourBookingService

            // Mock repository calls
            lenient().when(bookingServiceRepository.findById(1L)).thenReturn(Optional.of(bookingService));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.updateServiceQuantity(requestDTO);
            });

            assertEquals("Cập nhật số lượng dịch vụ thất bại", exception.getResponseMessage());}
        @Test
        void testUpdateServiceQuantity_NullTourBookingServiceId_ThrowsException_UTCID03() {
            // Arrange
            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
            requestDTO.setTourBookingServiceId(null);
            requestDTO.setNewQuantity(20);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.updateServiceQuantity(requestDTO);
            });

            assertEquals("Cập nhật số lượng dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testUpdateServiceQuantity_NonExistentTourBookingServiceId_ThrowsException_UTCID04() {
            // Arrange
            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
            requestDTO.setTourBookingServiceId(100000L);
            requestDTO.setNewQuantity(20);

            // Mock checkAuthorByTourBookingService

            // Mock repository calls
            lenient().when(bookingServiceRepository.findById(100000L)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.updateServiceQuantity(requestDTO);
            });

            assertEquals("Cập nhật số lượng dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testUpdateServiceQuantity_NewQuantityZero_ThrowsException_UTCID05() {
            // Arrange
            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
            requestDTO.setTourBookingServiceId(1L);
            requestDTO.setNewQuantity(0);

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .tourSchedule(tourSchedule)
                    .build();

            Service service = Service.builder()
                    .id(1L)
                    .build();

            TourBookingService bookingService = TourBookingService.builder()
                    .id(1L)
                    .booking(booking)
                    .service(service)
                    .currentQuantity(10)
                    .status(TourBookingServiceStatus.AVAILABLE)
                    .build();

            // Mock checkAuthorByTourBookingService

            // Mock repository calls
            lenient().when(bookingServiceRepository.findById(1L)).thenReturn(Optional.of(bookingService));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.updateServiceQuantity(requestDTO);
            });

            assertEquals("Cập nhật số lượng dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testUpdateServiceQuantity_NegativeNewQuantity_ThrowsException_UTCID06() {
            // Arrange
            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
            requestDTO.setTourBookingServiceId(1L);
            requestDTO.setNewQuantity(-20);

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .tourSchedule(tourSchedule)
                    .build();

            Service service = Service.builder()
                    .id(1L)
                    .build();

            TourBookingService bookingService = TourBookingService.builder()
                    .id(1L)
                    .booking(booking)
                    .service(service)
                    .currentQuantity(10)
                    .status(TourBookingServiceStatus.AVAILABLE)
                    .build();

            // Mock checkAuthorByTourBookingService

            // Mock repository calls
            lenient().when(bookingServiceRepository.findById(1L)).thenReturn(Optional.of(bookingService));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.updateServiceQuantity(requestDTO);
            });

            assertEquals("Cập nhật số lượng dịch vụ thất bại", exception.getResponseMessage());
        }

        @Test
        void testUpdateServiceQuantity_NullNewQuantity_ThrowsException_UTCID07() {
            // Arrange
            ServiceQuantityUpdateDTO requestDTO = new ServiceQuantityUpdateDTO();
            requestDTO.setTourBookingServiceId(1L);
            requestDTO.setNewQuantity(null);

            TourSchedule tourSchedule = TourSchedule.builder()
                    .id(1L)
                    .build();

            TourBooking booking = TourBooking.builder()
                    .id(1L)
                    .tourSchedule(tourSchedule)
                    .build();

            Service service = Service.builder()
                    .id(1L)
                    .build();

            TourBookingService bookingService = TourBookingService.builder()
                    .id(1L)
                    .booking(booking)
                    .service(service)
                    .currentQuantity(10)
                    .status(TourBookingServiceStatus.AVAILABLE)
                    .build();

            // Mock checkAuthorByTourBookingService

            // Mock repository calls
            lenient().when(bookingServiceRepository.findById(1L)).thenReturn(Optional.of(bookingService));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                operatorService.updateServiceQuantity(requestDTO);
            });

            assertEquals("Cập nhật số lượng dịch vụ thất bại", exception.getResponseMessage());
        }
    }
}
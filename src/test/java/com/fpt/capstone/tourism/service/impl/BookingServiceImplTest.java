package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.BookingRequestDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourBookingHistoryDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
import com.fpt.capstone.tourism.service.TourService;
import com.fpt.capstone.tourism.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
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

    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingHelper bookingHelper;
    @Mock
    private TourBookingCustomerService tourBookingCustomerService;

    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private UserService userService;

    private Tour mockTour;
    private PublicTourScheduleDTO mockScheduleDTO;
    private BookingRequestDTO mockBookingRequest;

    @BeforeEach
    void setUp() {
// Fake user ID và thông tin
        String fakeUsername = "testuser";
        Long fakeUserId = 35L;

        // Mock SecurityContextHolder
        Authentication authentication = Mockito.mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(fakeUsername);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // Mock user trong repository để getCurrentUserId() hoạt động
        User mockUser = new User();
        mockUser.setId(fakeUserId);
        mockUser.setUsername(fakeUsername);
        lenient().when(userRepository.findByUsername(fakeUsername)).thenReturn(Optional.of(mockUser));

        lenient().when(locationMapper.toPublicLocationDTO(Mockito.any())).thenReturn(new PublicLocationDTO());
        lenient().when(tourImageMapper.toPublicTourImageDTO(Mockito.any())).thenReturn(new PublicTourImageDTO());


        // Mock Tour
        mockTour = Tour.builder()
                .id(1L)
                .name("Amazing Vietnam")
                .numberDays(5)
                .numberNights(4)
                .privacy(String.valueOf(false))
                .departLocation(Location.builder().id(1L).build())
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
    private Page<TourBooking> mockTourBookingPage() {
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setName("Test Tour");
        TourImage tourImage = new TourImage();
        tourImage.setImageUrl("url.jpg");
        tour.setTourImages(List.of(tourImage));

        TourBooking booking = new TourBooking();
        booking.setId(1L);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setBookingCode("ABC123");
        booking.setTour(tour);
        booking.setStatus(TourBookingStatus.PENDING);
        booking.setTotalAmount(2000000D);
        booking.setExpiredAt(LocalDateTime.now().plusDays(1));

        return new PageImpl<>(List.of(booking));
    }

    @Test
    void viewTourBookingDetail_Success() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        lenient().when(tourService.findSameLocationPublicTour(any())).thenReturn(List.of());
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

//    @Test
//    void createBooking_Success() {
//        when(tourBookingCustomerMapper.toAdultEntity(any())).thenReturn(List.of());
//        when(tourBookingCustomerMapper.toChildrenEntity(any())).thenReturn(List.of());
//        when(tourBookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//        when(transactionRepository.save(any())).thenReturn(null);
//
//        GeneralResponse<?> response = bookingService.createBooking(mockBookingRequest);
//
//        assertNotNull(response);
//    }

    @Test
    void createBooking_Failure() {
        lenient().when(tourBookingRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                bookingService.createBooking(mockBookingRequest)
        );

        assertTrue(exception.getResponseMessage().contains("Database error"));
    }


    @Test
    void testViewListBookingHistory_defaultParams() {
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        // Gọi service
        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(0, 10, null, null, null);

        // Kiểm tra
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testViewListBookingHistory_HaGiangPendingAsc() {
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(1, 2, "Hà Giang", "PENDING", "asc");

        assertEquals(200, response.getCode());
    }
    @Test
    void testViewListBookingHistory_ToQuocDesc() {
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(1, 2, "tổ quốc", null, "desc");

        assertEquals(200, response.getCode());
    }
    @Test
    void testViewListBookingHistory_EmptyKeyword() {
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(0, 10, "", null, null);

        assertEquals(200, response.getCode());
    }
    @Test
    void testViewListBookingHistory_KeywordAbcXyz() {
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(0, 10, "abc xyz", null, null);

        assertEquals(200, response.getCode());
    }
    @Test
    void testViewListBookingHistory_InvalidPage() {
        assertThrows(BusinessException.class, () ->
                bookingService.viewListBookingHistory(-1, 10, null, null, null));
    }
    @Test
    void testViewListBookingHistory_InvalidSize() {
        assertThrows(BusinessException.class, () ->
                bookingService.viewListBookingHistory(0, -1, null, null, null));
    }
    @Test
    void testViewListBookingHistory_InvalidPaymentStatus() {
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        // Nếu logic xử lý sai status → trả về empty hoặc exception tùy theo code
        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(0, 10, null, "INVALID_STATUS", null);

        assertEquals(200, response.getCode());
    }
    @Test
    void testViewListBookingHistory_InvalidOrderDate() {
        // Nếu không hợp lệ thì fallback là desc?
        Mockito.when(tourBookingRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockTourBookingPage());

        GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> response =
                bookingService.viewListBookingHistory(0, 10, null, null, "abc");

        assertEquals(200, response.getCode());
    }

    @Test
    void testViewTourBookingDetail_ValidTourAndSchedule() {
        Mockito.when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        Mockito.when(tourScheduleRepository.findTourScheduleByTourId(1L, 1L)).thenReturn(mockScheduleDTO);
        lenient().when(locationMapper.toPublicLocationDTO(Mockito.any())).thenReturn(new PublicLocationDTO());
        lenient().when(tourImageMapper.toPublicTourImageDTO(Mockito.any())).thenReturn(new PublicTourImageDTO());

        GeneralResponse<TourBookingDataResponseDTO> response = bookingService.viewTourBookingDetail(1L, 1L);

        assertEquals(200, response.getCode());
        assertEquals("Customer Tour Booking detail loaded successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
    }
    @Test
    void testViewTourBookingDetail_TourIdZero_NotFound() {
        Mockito.when(tourRepository.findById(0L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> bookingService.viewTourBookingDetail(0L, 1L));
    }
    @Test
    void testViewTourBookingDetail_TourIdNull() {
        assertThrows(BusinessException.class, () -> bookingService.viewTourBookingDetail(null, 1L));
    }
    @Test
    void testViewTourBookingDetail_TourNotExist() {
        Mockito.when(tourRepository.findById(100000L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> bookingService.viewTourBookingDetail(100000L, 1L));
    }
    @Test
    void testViewTourBookingDetail_ScheduleNotExist() {
        Mockito.when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        Mockito.when(tourScheduleRepository.findTourScheduleByTourId(1L, 100000L))
                .thenThrow(new RuntimeException("Schedule not found"));

        assertThrows(BusinessException.class, () -> bookingService.viewTourBookingDetail(1L, 100000L));
    }
    @Test
    void testViewTourBookingDetail_ScheduleIdNull() {
        Mockito.when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        Mockito.when(tourScheduleRepository.findTourScheduleByTourId(1L, null))
                .thenThrow(new RuntimeException("Schedule is null"));

        assertThrows(BusinessException.class, () -> bookingService.viewTourBookingDetail(1L, null));
    }
    @Test
    void testViewTourBookingDetail_ScheduleIdZero() {
        Mockito.when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        Mockito.when(tourScheduleRepository.findTourScheduleByTourId(1L, 0L))
                .thenThrow(new RuntimeException("Invalid schedule"));

        assertThrows(BusinessException.class, () -> bookingService.viewTourBookingDetail(1L, 0L));
    }

//    @Test
//    void testCreateBooking_WithValidInput_ShouldReturnBookingCode() {
//        // Given
//        BookingRequestDTO requestDTO = BookingRequestDTO.builder()
//                .userId(35L)
//                .tourId(1L)
//                .scheduleId(1L)
//                .fullName("Nguyen Van A")
//                .note("Need luxury hotel")
//                .phone("0975432765")
//                .address("Hà Nội")
//                .paymentMethod(PaymentMethod.BANKING)
//                .email("Anguyen@gmail.com")
//                .adults(List.of(
//                        new BookingRequestCustomerDTO("Nguyễn Văn A", Gender.MALE,  Date.from(LocalDate.of(2000, 9, 24).atStartOfDay(ZoneId.systemDefault()).toInstant()), false),
//                        new BookingRequestCustomerDTO("Nguyễn Thị A", Gender.FEMALE,  Date.from(LocalDate.of(2000, 9, 24).atStartOfDay(ZoneId.systemDefault()).toInstant()), true)
//                ))
//                .children(List.of(
//                        new BookingRequestCustomerDTO("Nguyễn Văn B", Gender.MALE,  Date.from(LocalDate.of(2000, 9, 24).atStartOfDay(ZoneId.systemDefault()).toInstant()), false)
//                ))
//                .total(13000000.0)
//                .sellingPrice(5000000.0)
//                .extraHotelCost(500000.0)
//                .build();
//
//        List<TourBookingCustomer> adultEntities = List.of(
//                TourBookingCustomer.builder().fullName("Nguyễn Văn A").ageType(AgeType.ADULT).build(),
//                TourBookingCustomer.builder().fullName("Nguyễn Thị A").ageType(AgeType.ADULT).build()
//        );
//        List<TourBookingCustomer> childEntities = List.of(
//                TourBookingCustomer.builder().fullName("Nguyễn Văn B").ageType(AgeType.CHILDREN).build()
//        );
//
//        TourBooking savedBooking = TourBooking.builder()
//                .id(99L)
//                .bookingCode("BK-001")
//                .build();
//
//        when(tourBookingCustomerMapper.toAdultEntity(anyList())).thenReturn(adultEntities);
//        when(tourBookingCustomerMapper.toChildrenEntity(anyList())).thenReturn(childEntities);
//        when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);
//
//        // When
//        GeneralResponse<?> response = bookingService.createBooking(requestDTO);
//
//        // Then
//        assertNotNull(response);
//    }




}

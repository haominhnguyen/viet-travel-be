package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.DashboardDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.common.TourDetailDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TagMapper;
import com.fpt.capstone.tourism.mapper.TourDayMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.mapper.TourMapper;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tag;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.TourImage;
import com.fpt.capstone.tourism.model.TourPax;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.*;
import com.fpt.capstone.tourism.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private TourDayRepository tourDayRepository;

    @Mock
    private TourPaxRepository tourPaxRepository;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private TourImageMapper tourImageMapper;

    @Mock
    private TourImageRepository tourImageRepository;
    @Mock
    private TourBookingRepository tourBookingRepository;

    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TourDayMapper tourDayMapper;
    @Mock
    private CostAccountRepository costAccountRepository;

    @InjectMocks
    private TourServiceImpl tourService;

    private Tour mockTour;
    private PublicTourDTO mockTourDTO;
    private User user;
    private List<TourDay> tourDays;
    private List<TourPax> tourPaxes;

    @BeforeEach
    void setUp() {
        // Set up common test objects
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setEmail("test@example.com");

        mockTour = new Tour();
        mockTour.setId(1L);
        mockTour.setName("Amazing Vietnam");
        mockTour.setHighlights("Amazing Vietnam Highlights");
        mockTour.setNumberDays(5);
        mockTour.setNumberNights(4);
        mockTour.setNote("Amazing Vietnam Notes");
        mockTour.setTourType(TourType.SIC);
        mockTour.setTourStatus(TourStatus.DRAFT);
        mockTour.setCreatedBy(user);
        mockTour.setDeleted(false);
        mockTour.setCreatedAt(LocalDateTime.now());
        mockTour.setUpdatedAt(LocalDateTime.now());

        // Set a depart location
        Location departLocation = new Location();
        departLocation.setId(10L);
        departLocation.setName("Test Departure Location");
        mockTour.setDepartLocation(departLocation);

        // Add locations to the tour
        Location location1 = new Location();
        location1.setId(1L);
        location1.setName("Test Location 1");

        Location location2 = new Location();
        location2.setId(2L);
        location2.setName("Test Location 2");

        mockTour.setLocations(Arrays.asList(location1, location2));

        // Create tags with full information
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Beach");
        tag1.setDescription("Beautiful beach destinations with white sand and blue sea.");
        tag1.setDeleted(false);
        tag1.setTours(new ArrayList<>());
        tag1.setBlogs(new ArrayList<>());

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Adventure");
        tag2.setDescription("Exciting adventure experiences for thrill seekers.");
        tag2.setDeleted(false);
        tag2.setTours(new ArrayList<>());
        tag2.setBlogs(new ArrayList<>());

        mockTour.setTags(new ArrayList<>(Arrays.asList(tag1, tag2)));

        // Create tour images
        List<TourImage> tourImages = new ArrayList<>();
        TourImage image = new TourImage();
        image.setId(1L);
        image.setImageUrl("http://example.com/image.jpg");
        image.setTour(mockTour);
        image.setDeleted(false);
        tourImages.add(image);
        mockTour.setTourImages(tourImages);

        // Create DTO representation
        List<TagDTO> tagDTOs = mockTour.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());

        mockTourDTO = PublicTourDTO.builder()
                .id(1L)
                .name("Amazing Vietnam")
                .numberDays(5)
                .numberNight(4)
                .tags(tagDTOs)
                .departLocation(null)
                .tourImages(Collections.emptyList())
                .priceFrom(100.0)
                .build();

        // Create tour days
        tourDays = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TourDay tourDay = new TourDay();
            tourDay.setId((long) i);
            tourDay.setDayNumber(i);
            tourDay.setTitle("Day " + i);
            tourDay.setContent("Day " + i + " content");
            tourDay.setTour(mockTour);
            tourDay.setDeleted(false);
            tourDays.add(tourDay);
        }

        // Create a tour pax configuration
        tourPaxes = new ArrayList<>();
        TourPax tourPax = new TourPax();
        tourPax.setId(1L);
        tourPax.setTour(mockTour);
        tourPax.setMinPax(2);
        tourPax.setMaxPax(10);
        tourPax.setSellingPrice(1000.0);
        tourPax.setDeleted(false);
        tourPaxes.add(tourPax);
    }

    // Tests for finding tours
    @Nested
    class FindTourTests {
        @Test
        void testFindTopTourOfYear_ReturnsNewestTourWhenNoTopTourFound() {
            when(tourRepository.findTopTourIdsOfCurrentYear()).thenReturn(Collections.emptyList());
            when(tourRepository.findNewestTour()).thenReturn(mockTour);
            when(tourRepository.findMinSellingPriceForTours(1L)).thenReturn(100.0);

            PublicTourDTO result = tourService.findTopTourOfYear();

            assertNotNull(result);
            assertEquals(mockTour.getId(), result.getId());
            assertEquals("Amazing Vietnam", result.getName());
            verify(tourRepository).findNewestTour();
        }

        @Test
        void testFindTopTourOfYear_ReturnsTopTour() {
            List<Long> topTourIds = List.of(1L);
            when(tourRepository.findTopTourIdsOfCurrentYear()).thenReturn(topTourIds);
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourRepository.findMinSellingPriceForTours(1L)).thenReturn(100.0);

            PublicTourDTO result = tourService.findTopTourOfYear();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Amazing Vietnam", result.getName());
            verify(tourRepository).findById(1L);
        }

        @Test
        void testFindTrendingTours_ReturnsTours() {
            List<Long> trendingTourIds = List.of(1L);
            mockTour.setTourStatus(TourStatus.APPROVED); // Ensure tour passes status filter
            mockTour.setTourType(TourType.SIC); // Ensure tour passes type filter
            List<Tour> trendingTours = List.of(mockTour);
            Pageable pageable = PageRequest.of(0, 5);

            lenient().when(tourRepository.findTrendingTourIds()).thenReturn(trendingTourIds);
            lenient().when(tourRepository.findAllById(trendingTourIds)).thenReturn(trendingTours);

            List<Object[]> priceData = new ArrayList<>();
            priceData.add(new Object[]{1L, 100.0});
            lenient().when(tourRepository.findMinSellingPrices(trendingTourIds)).thenReturn(priceData);

            // Add mocks for mapping dependencies
            lenient().when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
            lenient().when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());
            lenient().when(tagMapper.toDTO(any())).thenReturn(new TagDTO());
            lenient().when(tourImageMapper.toPublicTourImageDTO(any())).thenReturn(new PublicTourImageDTO());

            List<PublicTourDTO> result = tourService.findTrendingTours(5);

            assertNotNull(result);
            assertEquals(0, result.size());

        }

        @Test
        void testGetAllPublicTour_ReturnsPagedTours() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
            Page<Tour> tourPage = new PageImpl<>(List.of(mockTour), pageable, 1);
            lenient().when(tourRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tourPage);
            List<Object[]> priceData = new ArrayList<>();
            priceData.add(new Object[]{1L, 100.0});
            when(tourRepository.findMinSellingPrices(anyList())).thenReturn(priceData);
            GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "", null, null, null, null, null, null);
            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertNotNull(response.getData());
            assertEquals(1, response.getData().getTotal());
        }

        @Test
        void testFindSameLocationPublicTour_ReturnsTours() {
            List<Long> tourIds = List.of(1L);
            List<TagDTO> mockTags = Collections.emptyList();
            List<PublicTourImageDTO> mockImages = Collections.emptyList();

            when(tourRepository.findSameLocationTourIds(any())).thenReturn(tourIds);
            when(tourRepository.findByIdAndTourStatusAndTourType(anyLong(), any(), any())).thenReturn(mockTour);
            when(tagRepository.findTagsByTourId(1L)).thenReturn(Collections.emptyList());
            when(tourRepository.findMinSellingPriceForTours(1L)).thenReturn(100.0);
            when(tourImageRepository.findTourImagesByTourId(1L)).thenReturn(Collections.emptyList());
            when(locationMapper.toPublicLocationDTO(any())).thenReturn(null);

            List<PublicTourDTO> result = tourService.findSameLocationPublicTour(List.of(1L));

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
        }
    }

    // Tests for tour approval
    @Nested
    class TourApprovalTests {
        @Test
        void sendTourForApproval_Success() {
            // Arrange
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);
            when(tourRepository.save(any(Tour.class))).thenReturn(mockTour);

            // Act
            GeneralResponse<TourResponseDTO> response = tourService.sendTourForApproval(1L, user);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals("Tour đã được gửi phê duyệt thành công", response.getMessage());

            // Verify the tour status was updated to PENDING
            assertEquals(TourStatus.PENDING, mockTour.getTourStatus());

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourDayRepository).findByTourIdAndDeletedFalseOrderByDayNumber(1L);
            verify(tourPaxRepository).findByTourIdAndDeletedFalse(1L);
            verify(tourRepository).save(mockTour);
        }

        @Test
        void sendTourForApproval_TourNotFound() {
            // Arrange
            when(tourRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(999L, user);
            });

            assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Tour not found"));

            // Verify repository methods were called
            verify(tourRepository).findById(999L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_NotInDraftStatus() {
            // Arrange
            mockTour.setTourStatus(TourStatus.APPROVED);
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Only tours in DRAFT status can be sent for approval"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_NotTourCreator() {
            // Arrange
            User differentUser = new User();
            differentUser.setId(2L);
            differentUser.setUsername("otheruser");

            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, differentUser);
            });

            assertEquals(HttpStatus.FORBIDDEN.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Only the tour creator or administrators can send a tour for approval"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_MissingName() {
            // Arrange
            mockTour.setName("");
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertFalse(!exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("name"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_MissingLocations() {
            // Arrange
            mockTour.setLocations(Collections.emptyList());
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("locations"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_MissingDepartLocation() {
            // Arrange
            mockTour.setDepartLocation(null);
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("departLocation"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_MissingTourDays() {
            // Arrange
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(Collections.emptyList());
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("tourDays"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourDayRepository).findByTourIdAndDeletedFalseOrderByDayNumber(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_MissingPaxConfiguration() {
            // Arrange
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(Collections.emptyList());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertFalse(!exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("paxConfigurations"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourDayRepository).findByTourIdAndDeletedFalseOrderByDayNumber(1L);
            verify(tourPaxRepository).findByTourIdAndDeletedFalse(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_NonMatchingTourDayCount() {
            // Arrange
            mockTour.setNumberDays(7); // Doesn't match tourDays.size() which is 5
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("tourDays"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourDayRepository).findByTourIdAndDeletedFalseOrderByDayNumber(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }

        @Test
        void sendTourForApproval_MissingTourType() {
            // Arrange
            mockTour.setTourType(null);
            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(1L)).thenReturn(tourDays);
            when(tourPaxRepository.findByTourIdAndDeletedFalse(1L)).thenReturn(tourPaxes);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.sendTourForApproval(1L, user);
            });

            assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpCode());
//            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("tourType"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }
    }

    private Tour createTour(Long id, String name) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setName(name);
        tour.setNumberDays(3);
        tour.setNumberNights(2);
        tour.setTags(Collections.emptyList());
        tour.setDepartLocation(new Location()); // Mocked location
        tour.setTourImages(Collections.emptyList());
        tour.setTourStatus(TourStatus.APPROVED); // Ensure tour passes status filter
        tour.setTourType(TourType.SIC); // Ensure tour passes type filter
        return tour;
    }

    private PublicTourDTO createPublicTourDTO(Long id, String name, Double priceFrom) {
        return new PublicTourDTO(id, name, 3, 2, Collections.emptyList(),
                new PublicLocationDTO(), Collections.emptyList(), Collections.emptyList(), priceFrom);
    }
    // UTC101D: Valid inputs, page=1, size=20, keyword="Hà Giang"
    @Test
    public void testGetAllPublicTour_ValidInputs_HaGiang() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        Map<Long, Double> minPriceMap = new HashMap<>();
        minPriceMap.put(1L, 10000.0);
//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(Arrays.asList(new Object[]{1L, 10000.0}));

        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());
        lenient().when(tagMapper.toDTO(any())).thenReturn(new TagDTO());
        lenient().when(tourImageMapper.toPublicTourImageDTO(any())).thenReturn(new PublicTourImageDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, "Hà Giang", null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getPage());
        assertEquals(20, response.getData().getSize());
//        assertEquals(1L, response.getData().getTotal());
        assertEquals(1, response.getData().getItems().size());
        assertEquals("Hà Giang Tour", response.getData().getItems().get(0).getName());
//        assertEquals(10000.0, response.getData().getItems().get(0).getPriceFrom(), 0.001);
    }

    // UTC102D: Valid inputs, page=1, size=20, keyword="to quoc"
    @Test
    public void testGetAllPublicTour_ValidInputs_ToQuoc() {
        // Arrange
        Tour tour = createTour(1L, "Tổ Quốc Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, "to quoc", null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
        assertEquals("Tổ Quốc Tour", response.getData().getItems().get(0).getName());
    }

    // UTC103D: Invalid keyword, keyword="an_kh"
    @Test
    public void testGetAllPublicTour_InvalidKeyword() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, "an_kh", null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC104D: Invalid keyword, keyword=null
    @Test
    public void testGetAllPublicTour_NullKeyword() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC105D: Valid budgetFrom, budgetFrom=2000000
    @Test
    public void testGetAllPublicTour_ValidBudgetFrom() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 3000000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, 2000000.0, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
    }

    // UTC106D: Invalid budgetFrom, budgetFrom=0
    @Test
    public void testGetAllPublicTour_InvalidBudgetFrom() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, 0.0, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC107D: Invalid budgetFrom, budgetFrom=null
    @Test
    public void testGetAllPublicTour_NullBudgetFrom() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC108D: Valid budgetTo, budgetTo=5000000
    @Test
    public void testGetAllPublicTour_ValidBudgetTo() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 4000000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, 5000000.0, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
    }

    // UTC109D: Invalid budgetTo, budgetTo=MAX_VALUE
    @Test
    public void testGetAllPublicTour_MaxBudgetTo() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, (double) Long.MAX_VALUE, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC110D: Invalid budgetTo, budgetTo=null
    @Test
    public void testGetAllPublicTour_NullBudgetTo() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC111D: Valid duration, duration=3
    @Test
    public void testGetAllPublicTour_ValidDuration() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        tour.setNumberDays(3);
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, 3, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
    }

    // UTC112D: Invalid duration, duration=0
    @Test
    public void testGetAllPublicTour_InvalidDuration() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, 0, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC113D: Invalid duration, duration=null
    @Test
    public void testGetAllPublicTour_NullDuration() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC114D: Valid fromDate, fromDate=future (9/3/2025)
    @Test
    public void testGetAllPublicTour_ValidFromDate_Future() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, LocalDate.of(2025, 3, 9), null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
    }

    // UTC115D: Invalid fromDate, fromDate=past (9/3/2024)
    @Test
    public void testGetAllPublicTour_InvalidFromDate_Past() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, LocalDate.of(2024, 3, 9), null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC116D: Invalid fromDate, fromDate=null
    @Test
    public void testGetAllPublicTour_NullFromDate() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC117D: Valid departLocationId, departLocationId=1000
    @Test
    public void testGetAllPublicTour_ValidDepartLocationId() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, 1000L, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
    }

    // UTC118D: Invalid departLocationId, departLocationId=1
    @Test
    public void testGetAllPublicTour_InvalidDepartLocationId() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, 1L, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC119D: Invalid departLocationId, departLocationId=null
    @Test
    public void testGetAllPublicTour_NullDepartLocationId() {
        // Arrange
        Page<Tour> tourPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(0, response.getData().getItems().size());
    }

    // UTC120D: Valid sortByPrice, sortByPrice="asc"
    @Test
    public void testGetAllPublicTour_SortByPriceAsc() {
        // Arrange
        Tour tour1 = createTour(1L, "Tour 1");
        Tour tour2 = createTour(2L, "Tour 2");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour1, tour2), PageRequest.of(1, 20), 2);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        when(tourRepository.findMinSellingPrices(List.of(1L, 2L))).thenReturn(List.of(
                new Object[]{1L, 20000.0}, new Object[]{2L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(anyLong())).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, "asc");

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(2, response.getData().getItems().size());
        assertEquals("Tour 2", response.getData().getItems().get(0).getName()); // Cheaper tour first
        assertEquals("Tour 1", response.getData().getItems().get(1).getName());
    }

    // UTC121D: Valid sortByPrice, sortByPrice="desc"
    @Test
    public void testGetAllPublicTour_SortByPriceDesc() {
        // Arrange
        Tour tour1 = createTour(1L, "Tour 1");
        Tour tour2 = createTour(2L, "Tour 2");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour1, tour2), PageRequest.of(1, 20), 2);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

        when(tourRepository.findMinSellingPrices(List.of(1L, 2L))).thenReturn(List.of(
                new Object[]{1L, 20000.0}, new Object[]{2L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(anyLong())).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, "desc");

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(2, response.getData().getItems().size());
        assertEquals("Tour 1", response.getData().getItems().get(0).getName()); // More expensive tour first
        assertEquals("Tour 2", response.getData().getItems().get(1).getName());
    }

    // UTC122D: Invalid sortByPrice, sortByPrice=null
    @Test
    public void testGetAllPublicTour_NullSortByPrice() {
        // Arrange
        Tour tour = createTour(1L, "Hà Giang Tour");
        Page<Tour> tourPage = new PageImpl<>(List.of(tour), PageRequest.of(1, 20), 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);

//        when(tourRepository.findMinSellingPrices(List.of(1L))).thenReturn(List.of(new Object[]{1L, 10000.0}));
        when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
        when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());

        // Act
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(
                1, 20, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Thành công", response.getMessage());
        assertEquals(1, response.getData().getItems().size());
    }
    @Nested
    class ViewTourDetailTests {
        @Test
        void testViewTourDetail_ValidId_Success() { // UTC101D
            // Arrange
            // Ensure tourDays is not null
            List<TourDay> tourDays = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                TourDay tourDay = new TourDay();
                tourDay.setId((long) i);
                tourDay.setDayNumber(i);
                tourDay.setTitle("Day " + i);
                tourDay.setContent("Day " + i + " content");
                tourDay.setTour(mockTour);
                tourDay.setDeleted(false);
                tourDays.add(tourDay);
            }
            mockTour.setTourDays(tourDays); // Explicitly set tourDays to avoid null

            when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
            when(tourScheduleRepository.findTourScheduleBasicByTourId(1L)).thenReturn(Collections.emptyList());
            lenient().when(tourService.findSameLocationPublicTour(List.of(1L, 2L))).thenReturn(Collections.emptyList());
            when(locationMapper.toPublicLocationDTO(any())).thenReturn(new PublicLocationDTO());
            when(tagMapper.toDTO(any())).thenReturn(new TagDTO());
            when(tourImageMapper.toPublicTourImageDTO(any())).thenReturn(new PublicTourImageDTO());
            when(tourDayMapper.toPublicTourDayDTO(any())).thenReturn(new PublicTourDayDTO());

            // Act
            GeneralResponse<TourDetailDTO> response = tourService.getTourDetail(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals("Tải chi tiết tour thành công", response.getMessage());
            assertNotNull(response.getData());
            assertEquals(1L, response.getData().getId());
            assertEquals("Amazing Vietnam", response.getData().getName());
            assertEquals(5, response.getData().getNumberDays());
            assertEquals(4, response.getData().getNumberNight());
        }

        @Test
        void testViewTourDetail_TourNotFound() { // UTC102D
            // Arrange
            when(tourRepository.findById(100000L)).thenReturn(Optional.empty()); // Updated to findById

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.getTourDetail(100000L); // Updated to getTourDetail
            });

            assertEquals("Tải chi tiết tour thất bại", exception.getResponseMessage());
            verify(tourRepository).findById(100000L); // Updated to findById
        }

        @Test
        void testViewTourDetail_NullId() { // UTC104D
            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.getTourDetail(null); // Updated to getTourDetail
            });

            assertEquals("Tải chi tiết tour thất bại", exception.getResponseMessage());
        }

        @Test
        void testViewTourDetail_NegativeId() { // Boundary case to replace UTC103D
            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.getTourDetail(-1L); // Updated to getTourDetail
            });

            assertEquals("Tải chi tiết tour thất bại", exception.getResponseMessage());
            verify(tourRepository).findById(-1L); // Updated to findById
        }

        @Test
        void testViewTourDetail_ServerError() { // UTC105D
            // Arrange
            when(tourRepository.findById(1L)).thenThrow(new RuntimeException("Database error")); // Updated to findById

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                tourService.getTourDetail(1L); // Updated to getTourDetail
            });

            assertEquals("Tải chi tiết tour thất bại", exception.getResponseMessage());
            verify(tourRepository).findById(1L); // Updated to findById
        }
    }

}
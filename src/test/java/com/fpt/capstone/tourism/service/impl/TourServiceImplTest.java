package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.dto.response.TourResponseDTO;
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
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.TagRepository;
import com.fpt.capstone.tourism.repository.TourDayRepository;
import com.fpt.capstone.tourism.repository.TourImageRepository;
import com.fpt.capstone.tourism.repository.TourPaxRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TourDayMapper tourDayMapper;

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
            List<Tour> trendingTours = List.of(mockTour);
            Pageable pageable = PageRequest.of(0, 5);

            when(tourRepository.findTrendingTourIds()).thenReturn(trendingTourIds);
            when(tourRepository.findAllById(trendingTourIds)).thenReturn(trendingTours);

            // Create a properly typed List of Object arrays
            List<Object[]> priceData = new ArrayList<>();
            priceData.add(new Object[]{1L, 100.0});

            when(tourRepository.findMinSellingPrices(trendingTourIds)).thenReturn(priceData);

            List<PublicTourDTO> result = tourService.findTrendingTours(5);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
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
            assertEquals("Tour successfully sent for approval", response.getMessage());

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
            assertTrue(exception.getResponseMessage().contains("Tour not found"));

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
            assertTrue(exception.getResponseMessage().contains("Only tours in DRAFT status can be sent for approval"));

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
            assertTrue(exception.getResponseMessage().contains("Only the tour creator or administrators can send a tour for approval"));

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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
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
            assertTrue(exception.getResponseMessage().contains("Tour is missing required information"));
            assertTrue(exception.getResponseMessage().contains("tourType"));

            // Verify repository methods were called
            verify(tourRepository).findById(1L);
            verify(tourRepository, never()).save(any(Tour.class));
        }
    }
}
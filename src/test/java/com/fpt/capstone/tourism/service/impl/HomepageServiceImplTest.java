package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.HomepageDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomepageServiceImplTest {

    @InjectMocks
    private HomepageServiceImpl homepageService;

    @Mock
    private TourService tourService;
    @Mock
    private BlogService blogService;
    @Mock
    private ServiceService serviceService;
//    @Mock
//    private ActivityService activityService;
    @Mock
    private ServiceProviderService providerService;
    @Mock
    private LocationService locationService;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private BlogRepository blogRepository;
//    @Mock
//    private ActivityRepository activityRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private ServiceProviderRepository serviceProviderRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
//    @Mock
//    private ActivityMapper activityMapper;
    @Mock
    private LocationMapper locationMapper;
    @Mock
    private BlogMapper blogMapper;
    @Mock
    private ServiceProviderMapper serviceProviderMapper;
    @Mock
    private ServiceMapper serviceMapper;
    @Mock
    private TagMapper tagMapper;
    @Mock
    private TourImageMapper tourImageMapper;
    @Mock
    private TourDayMapper tourDayMapper;

    private PublicTourDTO mockTour;
    private BlogResponseDTO mockBlog;
    private ActivityDTO mockActivity;
    private PublicActivityDTO mockPublicActivity;
    private PublicLocationDTO mockLocation;
    private ServiceProvider mockServiceProvider;
    private Tour mockTourEntity;

    @BeforeEach
    void setUp() {
        mockTour = new PublicTourDTO();
        mockBlog = new BlogResponseDTO();
        mockActivity = new ActivityDTO();
        mockLocation = new PublicLocationDTO();
        mockServiceProvider = new ServiceProvider();
        mockTourEntity = new Tour();
    }

    @Test
    void viewHomepage_Success() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(5)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(5)).thenReturn(Collections.singletonList(mockBlog));
        when(serviceService.findRecommendedActivities(5)).thenReturn(Collections.singletonList(mockPublicActivity));
        when(locationService.findRecommendedLocations(5)).thenReturn(Collections.singletonList(mockLocation));

        GeneralResponse<HomepageDTO> response = homepageService.viewHomepage(5, 5, 5, 5);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Tải trang chủ thành công", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getNewBlogs().size());
        assertEquals(1, response.getData().getTrendingTours().size());
    }

    @Test
    void viewHomepage_Failure() {
        when(tourService.findTopTourOfYear()).thenThrow(new RuntimeException("Database error"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                homepageService.viewHomepage(5, 5, 5, 5));

        assertEquals("Tải trang chủ thất bại", exception.getMessage());
    }

    @Test
    void viewAllHotel_Success() {
        PagingDTO<List<PublicServiceProviderDTO>> pagingDTO = new PagingDTO<>();
        GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>> expectedResponse = new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);

        when(providerService.getAllHotel(1, 10, "hotel", 3)).thenReturn(expectedResponse);

        GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>> response = homepageService.viewAllHotel(1, 10, "hotel", 3);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Success", response.getMessage());
    }

//    @Test
//    void viewTourDetail_Success() {
//        Long tourId = 2L;
//
//        // Create mock location and set it to the tour
//        Location mockLocation = new Location();
//        mockLocation.setId(100L);
//        mockLocation.setName("Hanoi");
//
//        Tag mockTag = new Tag();
//        mockTag.setId(200L);
//        mockTag.setName("Adventure");
//
//        TourImage mockTourImage = new TourImage();
//        mockTourImage.setId(300L);
//        mockTourImage.setImageUrl("https://example.com/image.jpg");
//
//        TourDay mockTourDay = new TourDay();
//        mockTourDay.setId(400L);
//        mockTourDay.setTitle("Day 1: Explore the City");
//
//        mockTourEntity = new Tour();
//        mockTourEntity.setId(tourId);
//        mockTourEntity.setName("Test Tour");
//        mockTourEntity.setLocations(Collections.singletonList(mockLocation));
//        mockTourEntity.setTags(Collections.singletonList(mockTag));
//        mockTourEntity.setTourDays(Collections.singletonList(mockTourDay));
//        mockTourEntity.setTourImages(Collections.singletonList(mockTourImage));
//
//        when(tourRepository.findById(tourId)).thenReturn(Optional.of(mockTourEntity));
//        when(tourService.findSameLocationPublicTour(anyList())).thenReturn(Collections.singletonList(mockTour));
//        when(tourScheduleRepository.findTourScheduleBasicByTourId(tourId)).thenReturn(Collections.emptyList());
//
//        GeneralResponse<PublicTourDetailDTO> response = homepageService.viewTourDetail(tourId);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//        assertEquals("Tour detail loaded successfully", response.getMessage());
//    }



    @Test
    void viewTourDetail_Failure() {
        Long tourId = 1L;
        lenient().when(tourRepository.findById(tourId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                homepageService.viewTourDetail(tourId));

        assertEquals("Tải chi tiết tour thất bại", exception.getMessage());
    }

//    @Test
//    void viewPublicLocationDetail_Success() {
//        Long locationId = 1L;
//        Location location = new Location();
//        location.setId(locationId);
//        location.setName("Hanoi");
//
//        Blog mockBlog = new Blog();
//        mockBlog.setId(1L);
//        mockBlog.setTitle("Top 10 places to visit in Hanoi");
//
//        BlogResponseDTO mockBlogResponse = new BlogResponseDTO();
//        mockBlogResponse.setId(1L);
//        mockBlogResponse.setTitle("Top 10 places to visit in Hanoi");
//
//        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
//        when(tourService.findSameLocationPublicTour(Collections.singletonList(locationId)))
//                .thenReturn(Collections.singletonList(mockTour));
//
//        when(blogRepository.findBlogRelatedLocations(anyString()))
//                .thenReturn(Collections.singletonList(mockBlog));
//
//        when(blogMapper.toDTO(any(Blog.class))).thenReturn(mockBlogResponse);
//
//        when(activityRepository.findRelatedActivities(eq(locationId), anyInt()))
//                .thenReturn(Collections.singletonList(new Activity()));
//        when(activityMapper.toPublicActivityDTO(any())).thenReturn(new PublicActivityDTO());
//        when(locationService.findRecommendedLocations(anyInt()))
//                .thenReturn(Collections.singletonList(mockLocation));
//
//        GeneralResponse<PublicLocationDetailDTO> response = homepageService.viewPublicLocationDetail(locationId);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//        assertEquals("Location detail loaded successfully", response.getMessage());
//    }



    @Test
    void viewPublicLocationDetail_Failure() {
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                homepageService.viewPublicLocationDetail(locationId));

        assertEquals("Tải chi tiết địa điểm thất bại", exception.getMessage());
    }

//    @Test
//    void viewPublicHotelDetail_Success() {
//        Long hotelId = 1L;
//        when(serviceProviderRepository.findById(hotelId)).thenReturn(Optional.of(mockServiceProvider));
//        when(serviceRepository.findRoomsByProviderId(hotelId)).thenReturn(Collections.emptyList());
//        when(serviceRepository.findOtherServicesByProviderId(hotelId)).thenReturn(Collections.emptyList());
//
//        GeneralResponse<PublicHotelDetailDTO> response = homepageService.viewPublicHotelDetail(hotelId);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//        assertEquals("Hotel detail loaded successfully", response.getMessage());
//    }

    @Test
    void viewPublicHotelDetail_Failure() {
        Long hotelId = 1L;
        when(serviceProviderRepository.findById(hotelId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                homepageService.viewPublicHotelDetail(hotelId));

        assertEquals("Tải chi tiết khách sạn thất bại", exception.getMessage());
    }
    @Test
    void viewHomepage_ReturnSuccessDefault() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(3)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(3)).thenReturn(Collections.singletonList(mockBlog));
        when(serviceService.findRecommendedActivities(3)).thenReturn(Collections.singletonList(mockPublicActivity));
        when(locationService.findRecommendedLocations(7)).thenReturn(Collections.singletonList(mockLocation));

        GeneralResponse<HomepageDTO> response = homepageService.viewHomepage(3, 3, 3, 7);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
    }

    @Test
    void viewHomepage_ReturnSuccess() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(5)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(5)).thenReturn(Collections.singletonList(mockBlog));
        when(serviceService.findRecommendedActivities(5)).thenReturn(Collections.singletonList(mockPublicActivity));
        when(locationService.findRecommendedLocations(5)).thenReturn(Collections.singletonList(mockLocation));

        GeneralResponse<HomepageDTO> response = homepageService.viewHomepage(5, 5, 5, 5);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
    }
    @Test
    void viewHomepage_NumberTourNegative_ShouldThrowException() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(-1)).thenThrow(new RuntimeException("Tour error"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> homepageService.viewHomepage(-1, 3, 3, 3));
        assertFalse(exception.getMessage().contains("Homepage loaded fail"));
    }

    @Test
    void viewHomepage_NumberBlogNegative_ShouldThrowException() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(3)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(-1)).thenThrow(new RuntimeException("Blog error"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> homepageService.viewHomepage(3, -1, 3, 3));
        assertFalse(exception.getMessage().contains("Homepage loaded fail"));
    }
    @Test
    void viewHomepage_NumberLocationNegative_ShouldThrowException() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(3)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(3)).thenReturn(Collections.singletonList(mockBlog));
        when(serviceService.findRecommendedActivities(3)).thenReturn(Collections.singletonList(mockPublicActivity));
        when(locationService.findRecommendedLocations(-1)).thenThrow(new RuntimeException("Location error"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> homepageService.viewHomepage(3, 3, 3, -1));
        assertFalse(exception.getMessage().contains("Homepage loaded fail"));
    }

    @Test
    void viewHomepage_NumberActivityNegative_ShouldThrowException() {
        when(tourService.findTopTourOfYear()).thenReturn(mockTour);
        when(tourService.findTrendingTours(3)).thenReturn(Collections.singletonList(mockTour));
        when(blogService.findNewestBlogs(3)).thenReturn(Collections.singletonList(mockBlog));
        when(serviceService.findRecommendedActivities(-1)).thenThrow(new RuntimeException("Activity error"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> homepageService.viewHomepage(3, 3, -1, 3));
        assertFalse(exception.getMessage().contains("Homepage loaded fail"));
    }



}

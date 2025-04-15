package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.common.TourBasicDTO;
import com.fpt.capstone.tourism.dto.common.TourDetailDTO;
import com.fpt.capstone.tourism.dto.common.TourImageFullDTO;
import com.fpt.capstone.tourism.dto.request.TourImageRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TagService;
import com.fpt.capstone.tourism.service.TourDayServiceI;
import com.fpt.capstone.tourism.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TourManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TourService tourService;

    @Mock
    private TourDayServiceI tourDayServiceI;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private TagService tagService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private java.security.Principal principal;

    @InjectMocks
    private TourManagementController tourManagementController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create an argument resolver for @AuthenticationPrincipal
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails; // Return the mocked UserDetails
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(tourManagementController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();

        // Configure ObjectMapper for handling LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetAllTours() throws Exception {
        // Arrange
        List<TourBasicDTO> tours = new ArrayList<>();
        tours.add(createTourBasicDTO());

        PagingDTO<List<TourBasicDTO>> pagingDTO = PagingDTO.<List<TourBasicDTO>>builder()
                .page(0)
                .size(10)
                .total(1)
                .items(tours)
                .build();

        GeneralResponse<PagingDTO<List<TourBasicDTO>>> response =
                new GeneralResponse<>(200, "Tours retrieved successfully", pagingDTO);

        when(tourService.getAllTours(anyString(), anyBoolean(), anyBoolean(), any(Pageable.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/list")
                        .param("keyword", "test")
                        .param("isDeleted", "false")
                        .param("isOpened", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tours retrieved successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Test Tour"));
    }

    @Test
    void testGetTourById() throws Exception {
        // Arrange
        TourDetailDTO tourDetailDTO = createTourDetailDTO();

        GeneralResponse<TourDetailDTO> response =
                new GeneralResponse<>(200, "Tour retrieved successfully", tourDetailDTO);

        when(tourService.getTourDetail(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Tour"))
                .andExpect(jsonPath("$.data.numberDays").value(3))
                .andExpect(jsonPath("$.data.numberNight").value(2))
                .andExpect(jsonPath("$.data.tourType").value("GROUP"));
    }

    @Test
    void testGetTourWithActiveSchedule() throws Exception {
        // Arrange
        TourDetailDTO tourDetailDTO = createTourDetailDTO();

        GeneralResponse<TourDetailDTO> response =
                new GeneralResponse<>(200, "Tour with active schedule retrieved successfully", tourDetailDTO);

        when(tourService.getTourWithActiveSchedule(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/detail-schedule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour with active schedule retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Tour"));
    }

    @Test
    void testCreateTour() throws Exception {
        // Arrange
        TourRequestDTO requestDTO = createTourRequestDTO();
        TourResponseDTO responseDTO = createTourResponseDTO();

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        GeneralResponse<TourResponseDTO> response =
                new GeneralResponse<>(201, "Tour created successfully", responseDTO);

        // Set up userDetails mock to be returned by the AuthenticationPrincipal resolver
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourService.createTour(any(TourRequestDTO.class), any(User.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/head-of-business/tour/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Tour created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Tour"))
                .andExpect(jsonPath("$.data.tourType").value("SIC"));
    }

    @Test
    void testSendTourForApproval() throws Exception {
        // Arrange
        TourResponseDTO responseDTO = createTourResponseDTO();
        responseDTO.setTourStatus("PENDING_APPROVAL");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        GeneralResponse<TourResponseDTO> response =
                new GeneralResponse<>(200, "Tour sent for approval successfully", responseDTO);

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourService.sendTourForApproval(1L, user)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/head-of-business/tour/send-for-approval/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour sent for approval successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.tourStatus").value("PENDING_APPROVAL"));
    }

    @Test
    void testUpdateTour() throws Exception {
        // Arrange
        TourRequestDTO requestDTO = createTourRequestDTO();
        TourResponseDTO responseDTO = createTourResponseDTO();

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        GeneralResponse<TourResponseDTO> response =
                new GeneralResponse<>(200, "Tour updated successfully", responseDTO);

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tourService.updateTour(eq(1L), any(TourRequestDTO.class), any(User.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/head-of-business/tour/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Tour"));
    }

    @Test
    void testGetAllLocations() throws Exception {
        // Arrange
        List<LocationDTO> locations = Collections.singletonList(createLocationDTO());

        PagingDTO<List<LocationDTO>> pagingDTO = PagingDTO.<List<LocationDTO>>builder()
                .page(0)
                .size(10)
                .total(1)
                .items(locations)
                .build();

        GeneralResponse<PagingDTO<List<LocationDTO>>> response =
                new GeneralResponse<>(200, "Locations retrieved successfully", pagingDTO);

        when(locationService.getAllLocation(0, 10, "test", false, "desc"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/list-location")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "test")
                        .param("isDeleted", "false")
                        .param("orderDate", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Locations retrieved successfully"))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Test Location"));
    }

    @Test
    void testGetTags() throws Exception {
        // Arrange
        List<TagDTO> tags = Collections.singletonList(createTagDTO());

        GeneralResponse<List<TagDTO>> response =
                new GeneralResponse<>(200, "Tags retrieved successfully", tags);

        when(tagService.findAll()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/list-tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tags retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Adventure"));
    }

    // Helper methods to create test data
    private TourBasicDTO createTourBasicDTO() {
        List<TourImageFullDTO> images = Collections.singletonList(
                TourImageFullDTO.builder()
                        .id(1L)
                        .imageUrl("http://example.com/image1.jpg")
                        .deleted(false)
                        .build()
        );

        return TourBasicDTO.builder()
                .id(1L)
                .name("Test Tour")
                .highlights("Test Highlights")
                .numberDays(3)
                .numberNight(2)
                .note("Test Note")
                .deleted(false)
                .tourType(TourType.SIC)
                .tourStatus(TourStatus.DRAFT)
                .markUpPercent(10.0)
                .privacy("PUBLIC")
                .createdUserId(1L)
                .createdUserName("testuser")
                .tourImages(images)
                .build();
    }

    private TourDetailDTO createTourDetailDTO() {
        List<PublicLocationDTO> locations = Collections.singletonList(createPublicLocationDTO());
        List<TagDTO> tags = Collections.singletonList(createTagDTO());
        List<PublicTourScheduleDTO> schedules = Collections.singletonList(createPublicTourScheduleDTO());
        List<PublicTourImageDTO> images = Collections.singletonList(createPublicTourImageDTO());
        List<PublicTourDayDTO> days = Collections.singletonList(createPublicTourDayDTO());

        return TourDetailDTO.builder()
                .id(1L)
                .name("Test Tour")
                .highlights("Test Highlights")
                .numberDays(3)
                .numberNight(2)
                .note("Test Note")
                .privacy("PUBLIC")
                .tourType("GROUP")
                .locations(locations)
                .tags(tags)
                .departLocation(createPublicLocationDTO())
                .tourSchedules(schedules)
                .tourImages(images)
                .tourDays(days)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(createUserBasicDTO())
                .build();
    }

    private PublicLocationDTO createPublicLocationDTO() {
        return PublicLocationDTO.builder()
                .id(1L)
                .name("Test Location")
                .description("Test Description")
                .image("http://example.com/location.jpg")
                .geoPosition(createGeoPositionDTO())
                .build();
    }

    private GeoPositionDTO createGeoPositionDTO() {
        return GeoPositionDTO.builder()
                .id(1L)
                .latitude(10.5)
                .longitude(106.8)
                .build();
    }

    private TagDTO createTagDTO() {
        return TagDTO.builder()
                .id(1L)
                .name("Adventure")
                .build();
    }

    private PublicTourScheduleDTO createPublicTourScheduleDTO() {
        return PublicTourScheduleDTO.builder()
                .scheduleId(1L)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(13))
                .sellingPrice(1000.0)
                .minPax(5)
                .maxPax(15)
                .availableSeats(10)
                .meetingLocation("Hotel Lobby")
                .departureTime(LocalTime.of(8, 0))
                .extraHotelCost(50.0)
                .build();
    }

    private PublicTourImageDTO createPublicTourImageDTO() {
        return PublicTourImageDTO.builder()
                .id(1L)
                .imageUrl("http://example.com/image1.jpg")
                .build();
    }

    private PublicTourDayDTO createPublicTourDayDTO() {
        return PublicTourDayDTO.builder()
                .id(1L)
                .title("Day 1 - Arrival")
                .dayNumber(1)
                .content("Arrival and check-in to hotel")
                .mealPlan("Breakfast, Lunch, Dinner")
                .build();
    }

    private UserBasicDTO createUserBasicDTO() {
        return UserBasicDTO.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .avatarImage("http://example.com/avatar.jpg")
                .build();
    }

    private TourRequestDTO createTourRequestDTO() {
        List<Long> locationIds = Collections.singletonList(1L);
        List<Long> tagIds = Collections.singletonList(1L);
        List<TourImageRequestDTO> tourImages = Collections.singletonList(
                TourImageRequestDTO.builder()
                        .imageUrl("http://example.com/image1.jpg")
                        .build()
        );

        return TourRequestDTO.builder()
                .name("Test Tour")
                .highlights("Test Highlights")
                .numberDays(3)
                .numberNights(2)
                .note("Test Note")
                .locationIds(locationIds)
                .tagIds(tagIds)
                .tourType("SIC")
                .tourStatus("DRAFT")
                .departLocationId(1L)
                .markUpPercent(10.0)
                .privacy("PUBLIC")
                .tourImages(tourImages)
                .build();
    }

    private TourResponseDTO createTourResponseDTO() {
        List<PublicLocationDTO> locations = Collections.singletonList(createPublicLocationDTO());
        List<TagDTO> tags = Collections.singletonList(createTagDTO());
        List<PublicTourImageDTO> images = Collections.singletonList(createPublicTourImageDTO());

        return TourResponseDTO.builder()
                .id(1L)
                .name("Test Tour")
                .highlights("Test Highlights")
                .numberDays(3)
                .numberNights(2)
                .note("Test Note")
                .locations(locations)
                .tags(tags)
                .tourType("SIC")
                .tourStatus("DRAFT")
                .departLocation(createPublicLocationDTO())
                .markUpPercent(10.0)
                .privacy("PUBLIC")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .createdBy(createUserBasicDTO())
                .tourImages(images)
                .build();
    }

    private LocationDTO createLocationDTO() {
        return LocationDTO.builder()
                .id(1L)
                .name("Test Location")
                .description("Test Description")
                .image("http://example.com/location.jpg")
                .deleted(false)
                .geoPosition(createGeoPositionDTO())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
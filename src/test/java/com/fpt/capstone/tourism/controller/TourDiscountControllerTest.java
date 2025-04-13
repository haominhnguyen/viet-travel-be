package com.fpt.capstone.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TourDiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TourDiscountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TourDiscountService tourDiscountService;

    @Mock
    private LocationService locationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private TourDiscountController tourDiscountController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tourDiscountController).build();

        // Configure ObjectMapper for handling LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetTourServicesList() throws Exception {
        // Arrange
        TourServiceListDTO tourServiceListDTO = createMockTourServiceListDTO();

        when(tourDiscountService.getTourServicesList(eq(1L), isNull()))
                .thenReturn(new GeneralResponse<>(200, "Tour services retrieved successfully", tourServiceListDTO));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tour services retrieved successfully"))
                .andExpect(jsonPath("$.data.tourId").value(1))
                .andExpect(jsonPath("$.data.tourName").value("Sample Tour"))
                .andExpect(jsonPath("$.data.serviceCategories[0].categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data.serviceCategories[0].services[0].name").value("Luxury Hotel"));
    }

    @Test
    void testGetTourServicesListWithPaxCount() throws Exception {
        // Arrange
        TourServiceListDTO tourServiceListDTO = createMockTourServiceListDTO();

        when(tourDiscountService.getTourServicesList(eq(1L), eq(4)))
                .thenReturn(new GeneralResponse<>(200, "Tour services retrieved successfully", tourServiceListDTO));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/list")
                        .param("paxCount", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tourId").value(1));
    }

    @Test
    void testGetServiceDetailByDayAndService() throws Exception {
        // Arrange
        ServiceByCategoryDTO serviceByCategoryDTO = createMockServiceByCategoryDTO();

        when(tourDiscountService.getServiceDetailByDayAndService(eq(1L), eq(1), eq(1L)))
                .thenReturn(new GeneralResponse<>(200, "Service detail retrieved successfully", serviceByCategoryDTO));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/day/1/service/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service detail retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Luxury Hotel"))
                .andExpect(jsonPath("$.data.dayNumber").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Hotel"));
    }

    @Test
    void testGetDayNumbersByServiceAndTour() throws Exception {
        // Arrange
        List<Integer> dayNumbers = Arrays.asList(1, 2, 3);

        when(tourDiscountService.getDayNumbersByServiceAndTour(eq(1L), eq(1L)))
                .thenReturn(new GeneralResponse<>(200, "Day numbers retrieved successfully", dayNumbers));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/services/1/days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Day numbers retrieved successfully"))
                .andExpect(jsonPath("$.data[0]").value(1))
                .andExpect(jsonPath("$.data[1]").value(2))
                .andExpect(jsonPath("$.data[2]").value(3));
    }

    @Test
    void testGetServicesByProviderAndCategory() throws Exception {
        // Arrange
        ServiceProviderServicesDTO serviceProviderServicesDTO = createMockServiceProviderServicesDTO();

        when(tourDiscountService.getServicesByProviderAndCategory(eq(1L), eq("Hotel"), eq(1L)))
                .thenReturn(new GeneralResponse<>(200, "Services retrieved successfully", serviceProviderServicesDTO));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/provider/1/category/Hotel/location/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Services retrieved successfully"))
                .andExpect(jsonPath("$.data.providerId").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Hotel"))
                .andExpect(jsonPath("$.data.availableServices[0].name").value("Luxury Hotel"));
    }

    @Test
    void testCreateServiceDetail() throws Exception {
        // Arrange
        ServiceCreateRequestDTO createRequestDTO = createMockServiceCreateRequestDTO();
        ServiceByCategoryDTO serviceByCategoryDTO = createMockServiceByCategoryDTO();

        when(tourDiscountService.createServiceDetail(eq(1L), any(ServiceCreateRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Service detail created successfully", serviceByCategoryDTO));

        // Act & Assert
        mockMvc.perform(post("/head-of-business/tour/1/discount/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service detail created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Luxury Hotel"));
    }

    @Test
    void testUpdateServiceDetail() throws Exception {
        // Arrange
        ServiceUpdateRequestDTO updateRequestDTO = createMockServiceUpdateRequestDTO();
        ServiceByCategoryDTO serviceByCategoryDTO = createMockServiceByCategoryDTO();

        when(tourDiscountService.updateServiceDetail(eq(1L), eq(1L), any(ServiceUpdateRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Service detail updated successfully", serviceByCategoryDTO));

        // Act & Assert
        mockMvc.perform(put("/head-of-business/tour/1/discount/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service detail updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Luxury Hotel"));
    }

//    @Test
//    void testRemoveServiceFromTour() throws Exception {
//        // Arrange
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("testuser");
//
//        when(userDetails.getUsername()).thenReturn("testuser");
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//        when(tourDiscountService.removeServiceFromTour(eq(1L), eq(1L), eq(1)))
//                .thenReturn(new GeneralResponse<>(200, "Service removed successfully", null));
//
//        // Act & Assert
//        mockMvc.perform(delete("/head-of-business/tour/1/discount/remove/services/1")
//                        .param("dayNumber", "1")
//                        .principal((Principal) userDetails))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("Service removed successfully"));
//    }

    @Test
    void testGetServiceProviderOptions() throws Exception {
        // Arrange
        ServiceProviderOptionsDTO serviceProviderOptionsDTO = createMockServiceProviderOptionsDTO();

        when(tourDiscountService.getServiceProviderOptions(eq(1L), eq("Hotel")))
                .thenReturn(new GeneralResponse<>(200, "Service provider options retrieved successfully", serviceProviderOptionsDTO));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/providers")
                        .param("locationId", "1")
                        .param("categoryName", "Hotel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Service provider options retrieved successfully"));
    }

    @Test
    void testGetLocationsByTourId() throws Exception {
        // Arrange
        List<LocationDTO> locations = createMockLocationList();
        PagingDTO<List<LocationDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, locations);

        when(locationService.getLocationsByTourId(eq(1L), eq(0), eq(10), isNull(), isNull(), eq("desc")))
                .thenReturn(new GeneralResponse<>(200, "Locations retrieved successfully", pagingDTO));

        // Act & Assert
        mockMvc.perform(get("/head-of-business/tour/1/discount/list-location")
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderDate", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Locations retrieved successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Sample Location"));
    }

    // Helper methods to create mock data
    private TourServiceListDTO createMockTourServiceListDTO() {
        // Create mock service summary
        Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();
        paxPrices.put("1-2", PaxPriceInfoDTO.builder()
                .paxId(1L)
                .minPax(1)
                .maxPax(2)
                .paxRange("1-2")
                .price(100.0)
                .serviceNettPrice(80.0)
                .sellingPrice(120.0)
                .build());

        ServiceSummaryDTO serviceSummaryDTO = ServiceSummaryDTO.builder()
                .id(1L)
                .name("Luxury Hotel")
                .dayNumber(1)
                .status("ACTIVE")
                .nettPrice(80.0)
                .sellingPrice(100.0)
                .locationName("City Center")
                .locationId(1L)
                .serviceProviderName("Provider A")
                .serviceProviderId(1L)
                .paxPrices(paxPrices)
                .build();

        // Create mock tour service category DTO
        TourServiceCategoryDTO categoryDTO = TourServiceCategoryDTO.builder()
                .categoryName("Hotel")
                .services(Collections.singletonList(serviceSummaryDTO))
                .build();

        // Create mock tour pax option
        TourPaxOptionDTO paxOptionDTO = TourPaxOptionDTO.builder()
                .id(1L)
                .minPax(1)
                .maxPax(2)
                .paxRange("1-2")
                .price(100.0)
                .sellingPrice(120.0)
                .fixedCost(50.0)
                .extraHotelCost(20.0)
                .nettPricePerPax(80.0)
                .build();

        // Create mock tour service list DTO
        return TourServiceListDTO.builder()
                .tourId(1L)
                .tourName("Sample Tour")
                .tourType("GROUP")
                .totalDays(3)
                .serviceCategories(Collections.singletonList(categoryDTO))
                .paxOptions(Collections.singletonList(paxOptionDTO))
                .build();
    }

    private ServiceByCategoryDTO createMockServiceByCategoryDTO() {
        // Create mock pax price info
        Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();
        paxPrices.put("1", PaxPriceInfoDTO.builder()
                .paxId(1L)
                .minPax(1)
                .maxPax(2)
                .paxRange("1-2")
                .price(100.0)
                .serviceNettPrice(80.0)
                .sellingPrice(120.0)
                .build());

        // Create mock room detail
        RoomDetailDTO roomDetailDTO = RoomDetailDTO.builder()
                .id(1L)
                .capacity(2)
                .availableQuantity(10)
                .facilities("WiFi, TV, Mini Bar")
                .build();

        // Create service by category DTO
        return ServiceByCategoryDTO.builder()
                .id(1L)
                .name("Luxury Hotel")
                .description("5-star luxury hotel")
                .dayNumber(1)
                .status("ACTIVE")
                .nettPrice(80.0)
                .sellingPrice(100.0)
                .locationId(1L)
                .locationName("City Center")
                .serviceProviderId(1L)
                .serviceProviderName("Provider A")
                .categoryName("Hotel")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .paxPrices(paxPrices)
                .roomDetail(roomDetailDTO)
                .build();
    }

    private ServiceProviderServicesDTO createMockServiceProviderServicesDTO() {
        // Create mock room detail
        RoomDetailDTO roomDetailDTO = RoomDetailDTO.builder()
                .id(1L)
                .capacity(2)
                .availableQuantity(10)
                .facilities("WiFi, TV, Mini Bar")
                .build();

        // Create mock available service
        AvailableServiceDTO availableServiceDTO = AvailableServiceDTO.builder()
                .id(1L)
                .name("Luxury Hotel")
                .categoryName("Hotel")
                .nettPrice(80.0)
                .sellingPrice(100.0)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .providerId(1L)
                .providerName("Provider A")
                .roomDetail(roomDetailDTO)
                .build();

        // Create service provider services DTO
        return ServiceProviderServicesDTO.builder()
                .categoryId(1L)
                .categoryName("Hotel")
                .providerId(1L)
                .providerName("Provider A")
                .locationId(1L)
                .locationName("City Center")
                .availableServices(Collections.singletonList(availableServiceDTO))
                .build();
    }

    private ServiceCreateRequestDTO createMockServiceCreateRequestDTO() {
        // Create mock pax prices map
        Map<String, Double> paxPrices = new HashMap<>();
        paxPrices.put("1", 100.0);
        paxPrices.put("2", 90.0);

        // Create mock room detail
        RoomDetailDTO roomDetailDTO = RoomDetailDTO.builder()
                .id(1L)
                .capacity(2)
                .availableQuantity(10)
                .facilities("WiFi, TV, Mini Bar")
                .build();

        // Create service create request DTO
        return ServiceCreateRequestDTO.builder()
                .serviceId(1L)
                .locationId(1L)
                .serviceProviderId(1L)
                .dayNumber(1)
                .quantity(2)
                .sellingPrice(100.0)
                .nettPrice(80.0)
                .paxPrices(paxPrices)
                .roomDetail(roomDetailDTO)
                .build();
    }

    private ServiceUpdateRequestDTO createMockServiceUpdateRequestDTO() {
        // Create mock pax prices map
        Map<Long, Double> paxPrices = new HashMap<>();
        paxPrices.put(1L, 110.0);
        paxPrices.put(2L, 100.0);

        // Create service update request DTO
        return ServiceUpdateRequestDTO.builder()
                .dayNumber(1)
                .locationId(1L)
                .serviceProviderId(1L)
                .serviceId(1L)
                .nettPrice(85.0)
                .sellingPrice(110.0)
                .paxPrices(paxPrices)
                .build();
    }

    private ServiceProviderOptionsDTO createMockServiceProviderOptionsDTO() {
        ServiceProviderOptionsDTO mockDTO = new ServiceProviderOptionsDTO();

        return mockDTO;
    }


    private List<LocationDTO> createMockLocationList() {
        // Create mock location DTO
        LocationDTO locationDTO = LocationDTO.builder()
                .id(1L)
                .name("Sample Location")
                .description("Sample location description")
                .build();

        return Collections.singletonList(locationDTO);
    }
}

//package com.fpt.capstone.tourism.service.impl;
//
//
//import com.fpt.capstone.tourism.dto.common.*;
//import com.fpt.capstone.tourism.dto.response.PagingDTO;
//import com.fpt.capstone.tourism.helper.PasswordGenerateImpl;
//import com.fpt.capstone.tourism.mapper.ServiceCategoryMapper;
//import com.fpt.capstone.tourism.mapper.ServiceProviderMapper;
//import com.fpt.capstone.tourism.model.*;
//import com.fpt.capstone.tourism.repository.*;
//import com.fpt.capstone.tourism.service.EmailConfirmationService;
//import com.fpt.capstone.tourism.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class ServiceProviderServiceImplTest {
//
//    @InjectMocks
//    private ServiceProviderServiceImpl serviceProviderService;
//
//    @Mock
//    private ServiceProviderRepository serviceProviderRepository;
//
//    @Mock
//    private LocationRepository locationRepository;
//
//    @Mock
//    private ServiceProviderMapper serviceProviderMapper;
//
//    @Mock
//    private ServiceCategoryMapper serviceCategoryMapper;
//
//    @Mock
//    private RoleRepository roleRepository;
//
//    @Mock
//    private UserRoleRepository userRoleRepository;
//
//    @Mock
//    private EmailConfirmationService emailConfirmationService;
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private PasswordGenerateImpl passwordGenerate;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private ServiceRepository serviceRepository;
//
//    private ServiceProvider mockServiceProvider;
//    private ServiceProviderDTO mockServiceProviderDTO;
//
//    @BeforeEach
//    void setUp() {
//        mockServiceProvider = new ServiceProvider();
//        mockServiceProvider.setId(1L);
//        mockServiceProvider.setName("Test Provider");
//        mockServiceProvider.setEmail("test@example.com");
//        mockServiceProvider.setPhone("123456789");
//        mockServiceProvider.setAddress("123 Street");
//        mockServiceProvider.setDeleted(false);
//
//        mockServiceProviderDTO = new ServiceProviderDTO();
//        mockServiceProviderDTO.setId(1L);
//        mockServiceProviderDTO.setName("Test Provider");
//        mockServiceProviderDTO.setEmail("test@example.com");
//        mockServiceProviderDTO.setPhone("123456789");
//        mockServiceProviderDTO.setAddress("123 Street");
//    }
//
//    @Test
//    void saveServiceProvider_Success() {
//        // Setting up mockServiceProviderDTO with required data
//        mockServiceProviderDTO.setImageUrl("test.jpg");
//        mockServiceProviderDTO.setAbbreviation("Test Provider");
//        mockServiceProviderDTO.setName("Test Provider");
//        mockServiceProviderDTO.setPhone("0328922222");
//        mockServiceProviderDTO.setAddress("123 Street");
//        mockServiceProviderDTO.setWebsite("www.fpt.com");
//
//        // Setting up GeoPositionDTO
//        GeoPositionDTO geoPositionDTO = new GeoPositionDTO(1L, 1.2, 1.3);
//        mockServiceProviderDTO.setGeoPosition(geoPositionDTO);
//
//        // Setting up LocationDTO
//        LocationDTO locationDTO = new LocationDTO();
//        locationDTO.setId(1L);
//        locationDTO.setName("Test Location");
//        locationDTO.setDescription("Test Description");
//        locationDTO.setImage("test.jpg");
//        locationDTO.setDeleted(false);
//        locationDTO.setGeoPosition(geoPositionDTO);
//        mockServiceProviderDTO.setLocation(locationDTO);
//
//        // Setting up Service Categories
//        ServiceCategoryDTO serviceCategoryDTO = new ServiceCategoryDTO();
//        serviceCategoryDTO.setId(1L);
//        serviceCategoryDTO.setCategoryName("Test Category");
//
//        List<ServiceCategoryDTO> serviceCategoriesDTO = new ArrayList<>();
//        serviceCategoriesDTO.add(serviceCategoryDTO);
//        mockServiceProviderDTO.setServiceCategories(serviceCategoriesDTO);
//
//        // Setting up mockServiceProvider entity
//        GeoPosition geoPosition = new GeoPosition();
//        geoPosition.setId(1L);
//        geoPosition.setLatitude(1.2);
//        geoPosition.setLongitude(1.3);
//
//        Location location = new Location();
//        location.setId(1L);
//        location.setName("Test Location");
//        location.setDescription("Test Description");
//        location.setImage("test.jpg");
//        location.setDeleted(false);
//        location.setGeoPosition(geoPosition);
//
//        ServiceCategory serviceCategory = new ServiceCategory();
//        serviceCategory.setId(1L);
//        serviceCategory.setCategoryName("Test Category");
//
//        List<ServiceCategory> serviceCategories = new ArrayList<>();
//        serviceCategories.add(serviceCategory);
//
//        mockServiceProvider.setGeoPosition(geoPosition);
//        mockServiceProvider.setLocation(location);
//        mockServiceProvider.setServiceCategories(serviceCategories);
//
//        // Mocking repository and mapper behavior
//        when(serviceProviderRepository.findByEmail(anyString())).thenReturn(null);
//        when(serviceProviderRepository.findByPhone(anyString())).thenReturn(null);
//        when(serviceProviderMapper.toEntity(any())).thenReturn(mockServiceProvider);
//        when(serviceProviderRepository.save(any())).thenReturn(mockServiceProvider);
//        when(serviceProviderMapper.toDTO(any())).thenReturn(mockServiceProviderDTO);
//
//        // Call the service method
//        GeneralResponse<ServiceProviderDTO> response = serviceProviderService.save(mockServiceProviderDTO);
//
//        // Assertions
//        assertEquals(200, response.getStatus());
//        assertEquals("Test Provider", response.getData().getName());
//    }
//
//
//    @Test
//    void getServiceProviderById_Success() {
//        when(serviceProviderRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceProvider));
//        when(serviceProviderMapper.toDTO(any())).thenReturn(mockServiceProviderDTO);
//
//        GeneralResponse<ServiceProviderDTO> response = serviceProviderService.getServiceProviderById(1L);
//
//        assertEquals(200, response.getStatus());
//        assertEquals(1L, response.getData().getId());
//    }
//
//    @Test
//    void updateServiceProvider_Success() {
//        mockServiceProviderDTO.setImageUrl("test.jpg");
//        mockServiceProviderDTO.setAbbreviation("Test Provider");
//        mockServiceProviderDTO.setName("Test Provider");
//        mockServiceProviderDTO.setPhone("0328922222");
//        mockServiceProviderDTO.setAddress("123 Street");
//        mockServiceProviderDTO.setWebsite("www.fpt.com");
//
//        // Create and set GeoPositionDTO
//        GeoPositionDTO geoPositionDTO = new GeoPositionDTO(1L, 1.2, 1.3);
//        mockServiceProviderDTO.setGeoPosition(geoPositionDTO);
//
//        // Ensure mockServiceProvider also has a GeoPosition
//        GeoPosition geoPosition = new GeoPosition();
//        geoPosition.setId(1L);
//        geoPosition.setLatitude(1.2);
//        geoPosition.setLongitude(1.3);
//
//        LocationDTO locationDTO = new LocationDTO();
//        locationDTO.setId(1L);
//        locationDTO.setName("Test Location");
//        locationDTO.setDescription("Test Description");
//        locationDTO.setImage("test.jpg");
//        locationDTO.setDeleted(false);
//        locationDTO.setGeoPosition(geoPositionDTO);
//        mockServiceProviderDTO.setLocation(locationDTO);
//
//        Location location = new Location();
//        location.setId(1L);
//        location.setName("Test Location");
//        location.setDescription("Test Description");
//        location.setImage("test.jpg");
//        location.setDeleted(false);
//        location.setGeoPosition(geoPosition);
//
//        mockServiceProvider.setGeoPosition(geoPosition);
//        mockServiceProvider.setLocation(location);
//
//        ServiceCategory serviceCategory = new ServiceCategory();
//        serviceCategory.setId(1L);
//        serviceCategory.setCategoryName("Test Category");
//
//        ServiceCategoryDTO serviceCategoryDTO = new ServiceCategoryDTO();
//        serviceCategoryDTO.setId(1L);
//        serviceCategoryDTO.setCategoryName("Test Category");
//
//        List<ServiceCategory> serviceCategories = new ArrayList<>();
//        serviceCategories.add(serviceCategory);
//        List<ServiceCategoryDTO> serviceCategoriesDTO = new ArrayList<>();
//        serviceCategoriesDTO.add(serviceCategoryDTO);
//
//        mockServiceProvider.setServiceCategories(serviceCategories);
//        mockServiceProviderDTO.setServiceCategories(serviceCategoriesDTO);
//
//        when(serviceProviderRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceProvider));
//        lenient().when(serviceProviderMapper.toDTO(any())).thenReturn(mockServiceProviderDTO);
//        lenient().when(serviceProviderRepository.save(any())).thenReturn(mockServiceProvider);
//
//        GeneralResponse<ServiceProviderDTO> response = serviceProviderService.updateServiceProvider(1L, mockServiceProviderDTO);
//
//        assertEquals(200, response.getStatus());
//        assertEquals("Test Provider", response.getData().getName());
//    }
//
//
//
//    @Test
//    void deleteServiceProvider_Success() {
//        User mockUser = new User();
//        mockUser.setId(100L);
//
//        mockServiceProvider = new ServiceProvider();
//        mockServiceProvider.setId(1L);
//        mockServiceProvider.setUser(mockUser);
//        mockServiceProvider.setDeleted(true);
//
//        mockServiceProviderDTO = new ServiceProviderDTO();
//        mockServiceProviderDTO.setDeleted(true);
//
//        when(userRepository.findUserById(anyLong())).thenReturn(Optional.of(mockUser));
//
//        when(serviceProviderRepository.findById(anyLong())).thenReturn(Optional.of(mockServiceProvider));
//        when(serviceProviderRepository.save(any())).thenReturn(mockServiceProvider);
//        when(serviceProviderMapper.toDTO(any())).thenReturn(mockServiceProviderDTO);
//
//        GeneralResponse<ServiceProviderDTO> response = serviceProviderService.deleteServiceProvider(1L, true);
//
//        assertEquals(200, response.getStatus());
//        assertTrue(response.getData().getDeleted());
//    }
//
//    @Test
//    void getAllServiceProviders_Success() {
//        List<ServiceProvider> serviceProviders = List.of(mockServiceProvider);
//        Page<ServiceProvider> serviceProviderPage = new PageImpl<>(serviceProviders);
//        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//
//        lenient().when(serviceProviderRepository.findAll(any(Specification.class), eq(pageable)))
//                .thenReturn(serviceProviderPage);
//
//        when(serviceProviderMapper.toDTO(any())).thenReturn(mockServiceProviderDTO);
//
//        GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> response = serviceProviderService.getAllServiceProviders(0, 10, "Test", false, null);
//
//        assertEquals(200, response.getStatus());
//        assertEquals(1, response.getData().getItems().size());
//    }
//}
//

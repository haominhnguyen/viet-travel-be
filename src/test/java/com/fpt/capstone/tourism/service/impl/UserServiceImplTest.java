package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.request.UserCreationRequestDTO;
import com.fpt.capstone.tourism.dto.request.UserProfileRequestDTO;
import com.fpt.capstone.tourism.dto.response.UserFullInformationResponseDTO;
import com.fpt.capstone.tourism.dto.response.UserProfileResponseDTO;
import com.fpt.capstone.tourism.model.enums.Gender;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.JwtHelper;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import com.fpt.capstone.tourism.repository.EmailConfirmationTokenRepository;
import com.fpt.capstone.tourism.repository.RoleRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.repository.UserRoleRepository;
import com.fpt.capstone.tourism.service.CloudinaryService;
import com.fpt.capstone.tourism.mapper.UserFullInformationMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.FAIL_TO_SAVE_USER_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserFullInformationMapper userFullInformationMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role adminRole;
    private Role userRole;
    private UserRole testUserRole;
    private Set<UserRole> userRoles;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setGender(Gender.valueOf("MALE"));
        testUser.setPhone("1234567890");
        testUser.setAddress("Test Address");
        testUser.setAvatarImage("test-avatar.jpg");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setDeleted(false);
        testUser.setEmailConfirmed(true);

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("ADMIN");

        userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleName("USER");

        testUserRole = new UserRole();
        testUserRole.setId(1L);
        testUserRole.setUser(testUser);
        testUserRole.setRole(userRole);
        testUserRole.setDeleted(false);

        userRoles = new HashSet<>();
        userRoles.add(testUserRole);
        testUser.setUserRoles(userRoles);
    }

    @Test
    void generateToken_shouldReturnToken() {
        // Arrange
        String expectedToken = "test-token";
        when(jwtHelper.generateToken(testUser)).thenReturn(expectedToken);

        // Act
        String result = userService.generateToken(testUser);

        // Assert
        assertEquals(expectedToken, result);
        verify(jwtHelper).generateToken(testUser);
    }

    @Test
    void findById_shouldReturnUser() {
        // Arrange
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findById(1L);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findUserById(1L);
    }

    @Test
    void findById_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> userService.findById(999L));
        verify(userRepository).findUserById(999L);
    }

    @Test
    void findUserByUsername_shouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findUserByUsername("testuser");

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findUserByEmail_shouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findUserByEmail("test@example.com");

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void saveUser_shouldReturnSavedUser() {
        // Arrange
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        User result = userService.saveUser(testUser);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }

    @Test
    void saveUser_shouldThrowBusinessException_whenSaveFails() {
        // Arrange
        when(userRepository.save(testUser)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.saveUser(testUser));
        assertEquals(FAIL_TO_SAVE_USER_MESSAGE, exception.getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void getUserById_shouldReturnUserResponse() {
        // Arrange
        UserFullInformationResponseDTO userDTO = new UserFullInformationResponseDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");

        List<Role> roles = Collections.singletonList(userRole);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findRolesByUserId(1L)).thenReturn(roles);
        when(userFullInformationMapper.toDTO(testUser)).thenReturn(userDTO);

        // Act
        GeneralResponse<?> response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.GET_USER_SUCCESS_MESSAGE, response.getMessage());
        assertEquals(userDTO, response.getData());

        verify(userRepository).findById(1L);
        verify(roleRepository).findRolesByUserId(1L);
        verify(userFullInformationMapper).toDTO(testUser);
    }

    @Test
    void getUserProfile_shouldReturnUserProfileResponse() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        GeneralResponse<UserProfileResponseDTO> response = userService.getUserProfile("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.GET_PROFILE_SUCCESS, response.getMessage());

        UserProfileResponseDTO profileDTO = response.getData();
        assertEquals(testUser.getId(), profileDTO.getId());
        assertEquals(testUser.getUsername(), profileDTO.getUsername());
        assertEquals(testUser.getFullName(), profileDTO.getFullName());
        assertEquals(testUser.getEmail(), profileDTO.getEmail());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void updateUserProfile_shouldReturnUpdatedProfile() {
        // Arrange
        UserProfileRequestDTO requestDTO = new UserProfileRequestDTO();
        requestDTO.setFullName("Updated Name");
        requestDTO.setEmail("updated@example.com");
        requestDTO.setGender(Gender.valueOf("FEMALE"));
        requestDTO.setPhone("9876543210");
        requestDTO.setAddress("Updated Address");

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(testUser));

        // Act
        GeneralResponse<UserProfileResponseDTO> response = userService.updateUserProfile(1L, requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.UPDATE_PROFILE_SUCCESS, response.getMessage());

        UserProfileResponseDTO profileDTO = response.getData();
        assertEquals(requestDTO.getFullName(), profileDTO.getFullName());
        assertEquals(requestDTO.getEmail(), profileDTO.getEmail());
        assertEquals(requestDTO.getGender(), profileDTO.getGender());
        assertEquals(requestDTO.getPhone(), profileDTO.getPhone());
        assertEquals(requestDTO.getAddress(), profileDTO.getAddress());

        verify(userRepository).findUserById(1L);
        verify(userRepository).save(testUser);
    }


    @Test
    void getCurrentUser_shouldThrowException_whenNotAuthenticated() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getCurrentUser());
        assertEquals(Constants.Message.USER_NOT_AUTHENTICATED, exception.getMessage());
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
    }

    @Test
    void updateAvatar_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        MultipartFile file = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", "test".getBytes());
        String imageUrl = "https://example.com/avatar.jpg";

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(testUser));
        when(cloudinaryService.uploadAvatar(file, 1L)).thenReturn(imageUrl);

        // Act
        GeneralResponse<String> response = userService.updateAvatar(1L, file);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.UPDATE_AVATAR_SUCCESS, response.getMessage());
        assertEquals(imageUrl, response.getData());

        verify(userRepository).findUserById(1L);
        verify(cloudinaryService).uploadAvatar(file, 1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_shouldReturnSuccessResponse() {
        // Arrange
        UserCreationRequestDTO requestDTO = new UserCreationRequestDTO();
        requestDTO.setUsername("newuser8");
        requestDTO.setPassword("Password123@");
        requestDTO.setRePassword("Password123@");
        requestDTO.setFullName("New User");
        requestDTO.setEmail("newuser@example.com");
        requestDTO.setGender(Gender.valueOf("MALE"));
        requestDTO.setPhone("5555555555");
        requestDTO.setAddress("New Address");
        requestDTO.setRoleNames(Collections.singletonList("USER"));

        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername(requestDTO.getUsername());

        Role role = new Role();
        role.setId(2L);
        role.setRoleName("USER");

        UserRole userRole = new UserRole();
        userRole.setId(2L);
        userRole.setUser(newUser);
        userRole.setRole(role);

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(userRole);
        newUser.setUserRoles(userRoles);

        UserFullInformationResponseDTO responseDTO = new UserFullInformationResponseDTO();
        responseDTO.setId(2L);
        responseDTO.setUsername(requestDTO.getUsername());

        when(userRepository.existsByUsername(requestDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(requestDTO.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("encodedPassword");
        when(userFullInformationMapper.toEntity(requestDTO)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userFullInformationMapper.toDTO(newUser)).thenReturn(responseDTO);

        // Act
        GeneralResponse<?> response = userService.createUser(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.CREATE_USER_SUCCESS_MESSAGE, response.getMessage());
        assertEquals(responseDTO, response.getData());

        verify(userRepository).existsByUsername(requestDTO.getUsername());
        verify(userRepository).existsByEmail(requestDTO.getEmail());
        verify(userRepository).existsByPhone(requestDTO.getPhone());
        verify(passwordEncoder).encode(requestDTO.getPassword());
        verify(userFullInformationMapper).toEntity(requestDTO);
        verify(userRepository).save(newUser);
        verify(roleRepository).findByRoleName("USER");
        verify(userRoleRepository).saveAll(any());
        verify(userFullInformationMapper).toDTO(newUser);
    }

    @Test
    void updateUser_shouldReturnSuccessResponse() {
        // Arrange
        UserCreationRequestDTO requestDTO = new UserCreationRequestDTO();
        requestDTO.setUsername("testuser");
        requestDTO.setPassword("NewPassword123@");
        requestDTO.setRePassword("NewPassword123@");
        requestDTO.setFullName("Updated Full Name");
        requestDTO.setEmail("updated@example.com");
        requestDTO.setGender(Gender.valueOf("FEMALE"));
        requestDTO.setPhone("9999999999");
        requestDTO.setAddress("Updated Address");
        requestDTO.setRoleNames(Arrays.asList("USER", "ADMIN"));

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("ADMIN");

        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleName("USER");

        UserFullInformationResponseDTO responseDTO = new UserFullInformationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUsername(requestDTO.getUsername());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(requestDTO.getUsername())).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(requestDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(requestDTO.getPhone())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(requestDTO.getPassword(), testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("newEncodedPassword");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userFullInformationMapper.toDTO(testUser)).thenReturn(responseDTO);

        // Act
        GeneralResponse<?> response = userService.updateUser(1L, requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.UPDATE_USER_SUCCESS_MESSAGE, response.getMessage());
        assertEquals(responseDTO, response.getData());

        verify(userRepository).findById(1L);
        verify(userRepository).findByUsername(requestDTO.getUsername());
        verify(userRepository, times(1)).findByEmail(requestDTO.getEmail());
        verify(userRepository, times(1)).findByPhone(requestDTO.getPhone());
        verify(userRepository).save(testUser);
        verify(userFullInformationMapper).toDTO(testUser);
    }

    @Test
    void deleteUser_shouldReturnSuccessResponse() {
        // Arrange
        UserFullInformationResponseDTO responseDTO = new UserFullInformationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userFullInformationMapper.toDTO(testUser)).thenReturn(responseDTO);

        // Act
        GeneralResponse<UserFullInformationResponseDTO> response = userService.deleteUser(1L, true);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.GENERAL_SUCCESS_MESSAGE, response.getMessage());
        assertEquals(responseDTO, response.getData());
        assertTrue(testUser.getDeleted());

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(userFullInformationMapper).toDTO(testUser);
    }

    @Test
    void getAllUser_shouldReturnPaginatedUsers() {
        // Arrange
        int page = 0;
        int size = 10;
        String keyword = "test";
        Boolean isDeleted = false;
        String roleName = "USER";
        String sortField = "username";
        String sortDirection = "asc";

        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        List<Role> roles = Collections.singletonList(userRole);
        UserFullInformationResponseDTO userDTO = new UserFullInformationResponseDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(roleRepository.findRolesByUserId(1L)).thenReturn(roles);
        when(userFullInformationMapper.toDTO(testUser)).thenReturn(userDTO);

        // Act
        GeneralResponse<PagingDTO<List<UserFullInformationResponseDTO>>> response =
                userService.getAllUser(page, size, keyword, isDeleted, roleName, sortField, sortDirection);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(Constants.Message.GET_ALL_USER_SUCCESS_MESSAGE, response.getMessage());

        PagingDTO<List<UserFullInformationResponseDTO>> pagingDTO = response.getData();
        assertEquals(page, pagingDTO.getPage());
        assertEquals(size, pagingDTO.getSize());
        assertEquals(1, pagingDTO.getTotal());
        assertEquals(1, pagingDTO.getItems().size());

        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(roleRepository).findRolesByUserId(1L);
        verify(userFullInformationMapper).toDTO(testUser);
    }
}
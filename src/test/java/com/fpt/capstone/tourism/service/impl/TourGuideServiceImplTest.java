package com.fpt.capstone.tourism.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourGuideResponseDTO;
import com.fpt.capstone.tourism.model.enums.Gender;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.TourGuideMapper;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import com.fpt.capstone.tourism.repository.RoleRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.repository.UserRoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class TourGuideServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private TourGuideMapper tourGuideMapper;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRoleRepository userRoleRepository;

    @InjectMocks private TourGuideServiceImpl tourGuideService;

    private User mockUser;
    private TourGuideResponseDTO mockResponseDTO;
    private TourGuideRequestDTO mockRequestDTO;

    @BeforeEach
    void setUp() {
        // Mock User
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("John Doe");
        mockUser.setUsername("johndoe123");
        mockUser.setPassword("encodedPassword");
        mockUser.setEmail("johndoe@example.com");
        mockUser.setPhone("1234567890");
        mockUser.setDeleted(false);
        mockUser.setAddress("123 Main St");
        mockUser.setGender(Gender.MALE);

        // Mock UserRole with Role ID 10 (Tour Guide)
        Role tourGuideRole = new Role();
        tourGuideRole.setId(10L);
        tourGuideRole.setRoleName("Tour_Guide");
        UserRole userRole = new UserRole(null, mockUser, false, tourGuideRole);
        mockUser.setUserRoles(Collections.singleton(userRole));

        // Mock Response DTO
        mockResponseDTO = new TourGuideResponseDTO();
        mockResponseDTO.setId(1L);
        mockResponseDTO.setFullName("John Doe");
        mockResponseDTO.setRoles(Collections.singletonList("Tour_Guide"));

        // Mock Request DTO
        mockRequestDTO = new TourGuideRequestDTO();
        mockRequestDTO.setFullName("John Doe");
        mockRequestDTO.setUsername("johndoe123");
        mockRequestDTO.setPassword("StrongPass1@");
        mockRequestDTO.setEmail("johndoe@example.com");
        mockRequestDTO.setPhone("1234567890");
        mockRequestDTO.setAddress("123 Main St");
        mockRequestDTO.setGender(Gender.MALE);
    }


    @Test
    void getById_Success() {
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(mockUser));
        when(tourGuideMapper.toDTO(mockUser)).thenReturn(mockResponseDTO);

        GeneralResponse<?> response = tourGuideService.getById(1L);

        assertEquals(200, response.getStatus());
        assertEquals("John Doe", ((TourGuideResponseDTO) response.getData()).getFullName());
    }

    @Test
    void create_Success() {
        when(userRepository.existsByUsername("johndoe123")).thenReturn(false);
        when(userRepository.existsByEmail("johndoe@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(tourGuideMapper.toEntity(any(TourGuideRequestDTO.class))).thenReturn(mockUser);
        when(userRepository.save(any())).thenReturn(mockUser);
        when(roleRepository.findById(10L)).thenReturn(Optional.of(new Role(10L, "Tour_Guide", false)));
        when(tourGuideMapper.toDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<?> response = tourGuideService.create(mockRequestDTO);

        assertEquals(200, response.getStatus());
        assertEquals("John Doe", ((TourGuideResponseDTO) response.getData()).getFullName());
    }


    @Test
    void update_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);
        when(tourGuideMapper.toDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<?> response = tourGuideService.update(1L, mockRequestDTO);

        assertEquals(200, response.getStatus());
        assertEquals("John Doe", ((TourGuideResponseDTO) response.getData()).getFullName());
    }

    @Test
    void update_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> tourGuideService.update(1L, mockRequestDTO));
        assertEquals("Không tìm thấy người dùng", exception.getMessage());
    }

    @Test
    void delete_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);
        when(tourGuideMapper.toDTO(any())).thenReturn(mockResponseDTO);

        GeneralResponse<?> response = tourGuideService.delete(1L, true);

        assertEquals(200, response.getStatus());
    }

    @Test
    void delete_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> tourGuideService.delete(1L, true));
        assertEquals("Không tìm thấy người dùng", exception.getMessage());
    }
}

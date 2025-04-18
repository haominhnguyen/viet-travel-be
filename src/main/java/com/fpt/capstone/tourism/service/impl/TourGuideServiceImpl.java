package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourGuideResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.TourGuideMapper;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import com.fpt.capstone.tourism.repository.RoleRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.repository.UserRoleRepository;
import com.fpt.capstone.tourism.service.TourGuideService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;

@Service
@RequiredArgsConstructor
public class TourGuideServiceImpl implements TourGuideService {
    private final UserRepository userRepository;
    private final TourGuideMapper tourGuideMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    @Override
    public GeneralResponse<?> getById(Long id) {
        try {
            TourGuideResponseDTO response = userRepository.findUserById(id)
                    .filter(user -> user.getUserRoles().stream()
                            .anyMatch(role -> role.getRole().getId().equals(10L)))
                    .map(user -> {
                        TourGuideResponseDTO userDTO = tourGuideMapper.toDTO(user);
                        userDTO.setRoles(Collections.singletonList("Tour_Guide"));
                        return userDTO;
                    })
                    .orElseThrow(() -> BusinessException.of(USER_NOT_FOUND_MESSAGE));

            return GeneralResponse.of(response, GET_USER_SUCCESS_MESSAGE);
        } catch (BusinessException e) {
            return GeneralResponse.of(e);
        } catch (Exception e) {
            throw BusinessException.of(GET_USER_FAIL_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> create(TourGuideRequestDTO userDTO) {
        try {
            // Validate user input
            Validator.validateCreateTourGuideFields(
                    userDTO.getFullName(), userDTO.getUsername(), userDTO.getPassword(),
                    userDTO.getEmail(), String.valueOf(userDTO.getGender()), userDTO.getPhone(),
                    userDTO.getAddress());

            // Check for duplicate username
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw BusinessException.of(HttpStatus.CONFLICT, DUPLICATE_USERNAME_MESSAGE);
            }
            // Check for duplicate email
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw BusinessException.of(HttpStatus.CONFLICT, EMAIL_ALREADY_EXISTS_MESSAGE);
            }
            // Check for duplicate phone number
            if (userRepository.existsByPhone(userDTO.getPhone())) {
                throw BusinessException.of(HttpStatus.CONFLICT, PHONE_ALREADY_EXISTS_MESSAGE);
            }

            // Hash password before saving
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

            // Convert DTO to Entity
            User user = tourGuideMapper.toEntity(userDTO);
            user.setPassword(encodedPassword);
            user.setDeleted(false);
            user.setEmailConfirmed(true);

            // Save user first
            User savedUser = userRepository.save(user);

            // Assign Tour Guide role (ID = 10)
            Role tourGuideRole = roleRepository.findById(10L)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, ROLE_NOT_FOUND));
            UserRole userRole = new UserRole(null, savedUser, false, tourGuideRole);
            userRoleRepository.save(userRole);

            // Convert saved entity to response DTO
            TourGuideResponseDTO responseDTO = tourGuideMapper.toDTO(savedUser);
            responseDTO.setRoles(Collections.singletonList("Tour_Guide"));
            return GeneralResponse.of(responseDTO, TOUR_GUIDE_CREATED_SUCCESSFULLY);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(FAILED_TO_CREATE_TOUR_GUIDE, e);
        }

    }

    @Override
    @Transactional
    public GeneralResponse<?> update(Long id, TourGuideRequestDTO userDTO) {
        try {
            // Find user by ID
            User user = userRepository.findById(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));

            // Validate user input (password is now optional)
            Validator.validateUpdateTourGuideFields(
                    userDTO.getFullName(), userDTO.getUsername(), userDTO.getPassword(),
                    userDTO.getEmail(), String.valueOf(userDTO.getGender()), userDTO.getPhone(),
                    userDTO.getAddress());

            // Check for duplicate email and phone number (except for the current user)
            if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
                throw BusinessException.of(HttpStatus.CONFLICT, EMAIL_ALREADY_EXISTS_MESSAGE);
            }
            if (!user.getPhone().equals(userDTO.getPhone()) && userRepository.existsByPhone(userDTO.getPhone())) {
                throw BusinessException.of(HttpStatus.CONFLICT, PHONE_ALREADY_EXISTS_MESSAGE);
            }
            // Update fields
            user.setFullName(userDTO.getFullName());
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setGender(userDTO.getGender());
            user.setPhone(userDTO.getPhone());
            user.setAddress(userDTO.getAddress());
            user.setAvatarImage(userDTO.getAvatarImage());

            // Only update the password if a new one is provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                }
            }
            // Save updated user
            User updatedUser = userRepository.save(user);
            TourGuideResponseDTO responseDTO = tourGuideMapper.toDTO(updatedUser);
            responseDTO.setRoles(Collections.singletonList("Tour_Guide"));

            return GeneralResponse.of(responseDTO, TOUR_GUIDE_UPDATED_SUCCESSFULLY);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(FAILED_TO_UPDATE_TOUR_GUIDE, e);
        }

    }

    @Override
    @Transactional
    public GeneralResponse<?> delete(Long id, boolean isDeleted) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));
            user.setDeleted(isDeleted);

            User savedUser = userRepository.save(user);
            TourGuideResponseDTO responseDTO = tourGuideMapper.toDTO(savedUser);
            return GeneralResponse.of(responseDTO, TOUR_GUIDE_DELETED_SUCCESSFULLY);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(FAILED_TO_DELETE_TOUR_GUIDE, e);
        }
    }


    @Override
    public GeneralResponse<PagingDTO<List<TourGuideResponseDTO>>> getAll(
            int page, int size, String keyword, Boolean isDeleted, String roleName,
            String sortField, String sortDirection) {
        try {
            // Validate sortField to prevent invalid field names
            List<String> allowedSortFields = Arrays.asList("id", "createdAt", "username", "email");
            if (!allowedSortFields.contains(sortField)) {
                sortField = "createdAt";
            }
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<User> spec = buildSearchSpecification(keyword, isDeleted, roleName);

            // Get all users with role ID = 10 (Tour Guides)
            Page<User> userPage = userRepository.findAllTourGuides(spec,pageable);
            List<TourGuideResponseDTO> tourGuides = userPage.getContent().stream()
                    .map(user -> {
                        TourGuideResponseDTO userDTO = tourGuideMapper.toDTO(user);
                        userDTO.setRoles(Collections.singletonList("Tour Guide"));
                        return userDTO;
                    })
                    .collect(Collectors.toList());

            PagingDTO<List<TourGuideResponseDTO>> pagingDTO = PagingDTO.<List<TourGuideResponseDTO>>builder()
                    .page(page)
                    .size(size)
                    .total(userPage.getTotalElements())
                    .items(tourGuides)
                    .build();

            return GeneralResponse.of(pagingDTO, GET_ALL_USER_SUCCESS_MESSAGE);
        } catch (Exception e) {
            throw BusinessException.of(GET_ALL_USER_FAIL_MESSAGE, e);
        }
    }
    private Specification<User> buildSearchSpecification(String keyword, Boolean isDeleted, String roleName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedUsername = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("username")));
                Expression<String> normalizedEmail = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("email")));
                Expression<String> normalizedFullName = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("fullName")));
                Expression<String> normalizedPhone = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("phone")));
                Expression<String> normalizedAddress = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("address")));
                Expression<String> normalizedKeyword = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.literal(keyword.toLowerCase()));

                Predicate keywordPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(normalizedUsername, criteriaBuilder.concat("%", criteriaBuilder.concat(normalizedKeyword, "%"))),
                        criteriaBuilder.like(normalizedEmail, criteriaBuilder.concat("%", criteriaBuilder.concat(normalizedKeyword, "%"))),
                        criteriaBuilder.like(normalizedFullName, criteriaBuilder.concat("%", criteriaBuilder.concat(normalizedKeyword, "%"))),
                        criteriaBuilder.like(normalizedPhone, criteriaBuilder.concat("%", criteriaBuilder.concat(normalizedKeyword, "%"))),
                        criteriaBuilder.like(normalizedAddress, criteriaBuilder.concat("%", criteriaBuilder.concat(normalizedKeyword, "%")))
                );
                predicates.add(keywordPredicate);
            }

            // Filter by deleted status
            if (isDeleted != null) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), isDeleted));
            }

            // Filter by role name
            if (roleName != null && !roleName.isEmpty()) {
                Join<User, UserRole> userRoleJoin = root.join("userRoles", JoinType.INNER);
                Join<UserRole, Role> roleJoin = userRoleJoin.join("role", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(roleJoin.get("roleName"), roleName));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

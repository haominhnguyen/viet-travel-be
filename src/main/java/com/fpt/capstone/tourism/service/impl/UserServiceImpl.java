package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.UserProfileRequestDTO;
import com.fpt.capstone.tourism.dto.response.UserProfileResponseDTO;
import com.fpt.capstone.tourism.dto.request.UserCreationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.UserFullInformationResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.JwtHelper;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.UserFullInformationMapper;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import com.fpt.capstone.tourism.repository.EmailConfirmationTokenRepository;
import com.fpt.capstone.tourism.repository.RoleRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.CloudinaryService;
import com.fpt.capstone.tourism.repository.UserRoleRepository;
import com.fpt.capstone.tourism.service.UserService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import static com.fpt.capstone.tourism.constants.Constants.Message.*;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtHelper jwtHelper;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final UserFullInformationMapper userFullInformationMapper;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public String generateToken(User user) {
        return jwtHelper.generateToken(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findUserById(id).orElseThrow();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Transactional
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }


    @Override
    public User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw BusinessException.of(FAIL_TO_SAVE_USER_MESSAGE,e);
        }
    }

    @Override
    public Boolean existsByUsername(String userName) {
        return userRepository.existsByUsername(userName);
    }

    @Override
    public Boolean exitsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override

    public Boolean existsByPhoneNumber(String phone) {

        return userRepository.existsByPhone(phone);
    }


    @Override
    public GeneralResponse<?> getUserById(Long id) {
        try {
            UserFullInformationResponseDTO response = userRepository.findById(id)
                    .map(user -> {
                        // Fetch all roles of the user
                        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
                        UserFullInformationResponseDTO userDTO = userFullInformationMapper.toDTO(user);
                        userDTO.setRoles(roles.stream()
                                .map(Role::getRoleName)
                                .collect(Collectors.toList()));
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
    public void createEmailConfirmationToken(User user, String token) {
        // First, delete any existing tokens for this user
        emailConfirmationTokenRepository.deleteByUser(user);
        // Create and save the new token
        Token confirmationToken = new Token();
        confirmationToken.setToken(token);
        confirmationToken.setUser(user);
        confirmationToken.setCreatedAt(LocalDateTime.now());
        emailConfirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public User findUserByEmailConfirmationToken(String token) {
        Optional<Token> confirmationToken = emailConfirmationTokenRepository.findByToken(token);
        return confirmationToken.map(Token::getUser).orElse(null);
    }

    @Override
    @Transactional
    public void deleteEmailConfirmationToken(String token) {
        emailConfirmationTokenRepository.deleteByToken(token);
    }

    @Override
    public GeneralResponse<UserProfileResponseDTO> getUserProfile(String username) {
        try {


            User currentUser = userRepository.findByUsername(username).orElseThrow();

            UserProfileResponseDTO userProfileResponseDTO = UserProfileResponseDTO.builder()
                    .id(currentUser.getId())
                    .username(currentUser.getUsername())
                    .fullName(currentUser.getFullName())
                    .email(currentUser.getEmail())
                    .gender(currentUser.getGender())
                    .phone(currentUser.getPhone())
                    .address(currentUser.getAddress())
                    .avatarImg(currentUser.getAvatarImage())
                    .createAt(currentUser.getCreatedAt().toString())
                    .build();

            return GeneralResponse.of(userProfileResponseDTO, GET_PROFILE_SUCCESS);
        } catch (Exception e) {
            throw BusinessException.of(GET_PROFILE_FAIL);
        }
    }


    @Override
    public GeneralResponse<UserProfileResponseDTO> updateUserProfile(Long userId, UserProfileRequestDTO newUser) {

        try{
            User existingUser = findById(userId);

            //Check valid users field need to update
            Validator.validateProfile(newUser.getFullName(), newUser.getEmail(),
                    newUser.getPhone(), newUser.getAddress());

            //Update user follow by userProfileRequestDTO
            existingUser.setFullName(newUser.getFullName());
            existingUser.setEmail(newUser.getEmail());
            existingUser.setGender(newUser.getGender());
            existingUser.setPhone(newUser.getPhone());
            existingUser.setAddress(newUser.getAddress());

            userRepository.save(existingUser);

            UserProfileResponseDTO userProfileResponseDTO = UserProfileResponseDTO.builder()
                    .id(existingUser.getId())
                    .username(existingUser.getUsername())
                    .fullName(newUser.getFullName())
                    .email(newUser.getEmail())
                    .gender(newUser.getGender())
                    .phone(newUser.getPhone())
                    .address(newUser.getAddress())
                    .build();
            return GeneralResponse.of(userProfileResponseDTO, UPDATE_PROFILE_SUCCESS);
        } catch (Exception ex){
            throw BusinessException.of(UPDATE_PROFILE_FAIL, ex);
        }


    }

    @Override
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw BusinessException.of(Constants.Message.USER_NOT_AUTHENTICATED);
        }
        System.out.println(authentication.getPrincipal().toString());
        return authentication.getPrincipal().toString();
    }

    @Override
    public GeneralResponse<String> changePassword(String token, String currentPassword, String newPassword, String newRePassword) {
        try{
            String jwt = token.substring(7);
            String username = jwtHelper.extractUsername(jwt);

            //Find user from database
            User currentUser = findUserByUsername(username);

            //Check current password correct or not
            if(!passwordEncoder.matches(currentPassword, currentUser.getPassword())){
                throw BusinessException.of(Constants.Message.PASSWORDS_INCORRECT_MESSAGE);
            }

            //Valid new password
            if(!Validator.isPasswordValid(newPassword)){
                throw BusinessException.of(Constants.UserExceptionInformation.PASSWORD_INVALID);
            }

            if(!newPassword.equals(newRePassword)){
                throw BusinessException.of(Constants.Message.PASSWORDS_DO_NOT_MATCH_MESSAGE);
            }

            currentUser.setPassword(passwordEncoder.encode(newPassword));
            saveUser(currentUser);

            return new GeneralResponse<>(HttpStatus.OK.value(), CHANGE_PASSWORD_SUCCESS_MESSAGE, token);
        } catch (Exception e){
            throw BusinessException.of(Constants.Message.CHANGE_PASSWORD_FAIL_MESSAGE);
        }
    }

    @Override
    public GeneralResponse<String> updateAvatar(Long userId, MultipartFile file) {
        try {
            User user = findById(userId);

            String imageURL = cloudinaryService.uploadAvatar(file, userId);

            //Update imageURL in database
            user.setAvatarImage(imageURL);
            userRepository.save(user);
            return new GeneralResponse<>(HttpStatus.OK.value(), UPDATE_AVATAR_SUCCESS, imageURL);
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_AVATAR_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> createUser(UserCreationRequestDTO userDTO) {
        try {
            // Validate user input
            Validator.validateUserCreation(
                    userDTO.getFullName(), userDTO.getUsername(), userDTO.getPassword(), userDTO.getRePassword(),
                    userDTO.getEmail(), String.valueOf(userDTO.getGender()), userDTO.getPhone(),
                    userDTO.getAddress(), userDTO.getAvatarImage(), userDTO.getRoleNames());

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
            User user = userFullInformationMapper.toEntity(userDTO);
            user.setPassword(encodedPassword);
            user.setDeleted(false);
            user.setEmailConfirmed(true);

            // Save user first
            User savedUser = userRepository.save(user);

            // Assign roles
            Set<UserRole> userRoles = userDTO.getRoleNames().stream()
                    .map(roleName -> {
                        Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> BusinessException.of(HttpStatus.CONFLICT, ROLE_NOT_FOUND));
                        return new UserRole(null, savedUser, false, role);
                    })
                    .collect(Collectors.toSet());

            // Save roles and set them in user
            userRoleRepository.saveAll(userRoles);
            savedUser.setUserRoles(userRoles);

            // Convert saved entity to response DTO
            UserFullInformationResponseDTO responseDTO = userFullInformationMapper.toDTO(savedUser);

            return GeneralResponse.of(responseDTO, CREATE_USER_SUCCESS_MESSAGE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(CREATE_USER_FAIL_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> updateUser(Long id, UserCreationRequestDTO userDTO) {
        try {
            // Validate user input (password is now optional)
            Validator.validateUserUpdate(userDTO.getFullName(), userDTO.getUsername(),
                    userDTO.getPassword(), userDTO.getRePassword(), userDTO.getEmail(),
                    String.valueOf(userDTO.getGender()), userDTO.getPhone(),
                    userDTO.getAddress(), userDTO.getAvatarImage(), userDTO.getRoleNames());

            // Find user by ID
            User user = userRepository.findById(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));

            // Check for duplicate username (except for the current user)
            Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw BusinessException.of(HttpStatus.CONFLICT, DUPLICATE_USERNAME_MESSAGE);
            }

            // Check for duplicate email (except for the current user)
            Optional<User> existingEmailUser = userRepository.findByEmail(userDTO.getEmail());
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(id)) {
                throw BusinessException.of(HttpStatus.CONFLICT, EMAIL_ALREADY_EXISTS_MESSAGE);
            }

            // Check for duplicate phone number (except for the current user)
            Optional<User> existingPhoneUser = userRepository.findByPhone(userDTO.getPhone());
            if (existingPhoneUser.isPresent() && !existingPhoneUser.get().getId().equals(id)) {
                throw BusinessException.of(HttpStatus.CONFLICT, PHONE_ALREADY_EXISTS_MESSAGE);
            }

            // Only update fields if they have changed
            if (!user.getFullName().equals(userDTO.getFullName())) {
                user.setFullName(userDTO.getFullName());
            }
            if (!user.getUsername().equals(userDTO.getUsername())) {
                user.setUsername(userDTO.getUsername());
            }
            if (!user.getEmail().equals(userDTO.getEmail())) {
                user.setEmail(userDTO.getEmail());
            }
            if (!user.getPhone().equals(userDTO.getPhone())) {
                user.setPhone(userDTO.getPhone());
            }
            if (!user.getAddress().equals(userDTO.getAddress())) {
                user.setAddress(userDTO.getAddress());
            }

            //Only update the password if a new one is provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                }
            }

            // Only update roles if they have changed
            Set<String> currentRoleNames = user.getUserRoles().stream()
                    .map(userRole -> userRole.getRole().getRoleName())
                    .collect(Collectors.toSet());

            Set<String> newRoleNames = new HashSet<>(userDTO.getRoleNames());

            if (!currentRoleNames.equals(newRoleNames)) {
                Set<UserRole> newRoles = new HashSet<>();
                for (String roleName : userDTO.getRoleNames()) {
                    Role role = roleRepository.findByRoleName(roleName)
                            .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, ROLE_NOT_FOUND));
                    newRoles.add(new UserRole(null, user, false, role));
                }
                userRoleRepository.deleteByUserId(user.getId());
                userRoleRepository.saveAll(newRoles);
                user.setUserRoles(newRoles);
            }
            // Convert to response DTO
            UserFullInformationResponseDTO responseDTO = userFullInformationMapper.toDTO(userRepository.save(user));
            return GeneralResponse.of(responseDTO, UPDATE_USER_SUCCESS_MESSAGE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, UPDATE_USER_FAIL_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<UserFullInformationResponseDTO> deleteUser(Long id, boolean isDeleted) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));
            user.setDeleted(isDeleted);
            User savedUser = userRepository.save(user);
            UserFullInformationResponseDTO responseDTO = userFullInformationMapper.toDTO(savedUser);
            return GeneralResponse.of(responseDTO,GENERAL_SUCCESS_MESSAGE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, e);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<UserFullInformationResponseDTO>>> getAllUser(
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

            Page<User> userPage = userRepository.findAll(spec, pageable);
            List<UserFullInformationResponseDTO> users = userPage.getContent().stream()
                    .map(user -> {
                        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
                        UserFullInformationResponseDTO userDTO = userFullInformationMapper.toDTO(user);
                        userDTO.setRoles(roles.stream()
                                .map(Role::getRoleName)
                                .collect(Collectors.toList()));
                        return userDTO;
                    })
                    .collect(Collectors.toList());

            PagingDTO<List<UserFullInformationResponseDTO>> pagingDTO = PagingDTO.<List<UserFullInformationResponseDTO>>builder()
                    .page(page)
                    .size(size)
                    .total(userPage.getTotalElements())
                    .items(users)
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

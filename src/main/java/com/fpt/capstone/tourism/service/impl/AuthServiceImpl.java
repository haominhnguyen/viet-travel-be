package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.TokenDTO;
import com.fpt.capstone.tourism.dto.common.UserDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.RegisterRequestDTO;
import com.fpt.capstone.tourism.dto.response.UserInfoResponseDTO;
import com.fpt.capstone.tourism.enums.RoleName;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.JwtHelper;
import com.fpt.capstone.tourism.helper.TokenEncryptorImpl;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import com.fpt.capstone.tourism.repository.RoleRepository;
import com.fpt.capstone.tourism.repository.UserRoleRepository;
import com.fpt.capstone.tourism.service.AuthService;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final JwtHelper jwtHelper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfirmationService emailConfirmationService;
    private final TokenEncryptorImpl tokenEncryptor;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public GeneralResponse<TokenDTO> login(UserDTO userDTO) {

        try {
            Validator.validateLogin(userDTO.getUsername(), userDTO.getPassword());

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userDTO.getUsername(), userDTO.getPassword()
            ));
            User user = userService.findUserByUsername(userDTO.getUsername());

            if (Boolean.TRUE.equals(user.getDeleted())) {

                throw BusinessException.of(HttpStatus.FORBIDDEN.toString());
            }

            // Check if the user's email is confirmed
            if (!user.isEmailConfirmed()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, LOGIN_FAIL_MESSAGE);
            }
            String token = jwtHelper.generateToken(user);
            TokenDTO tokenDTO = TokenDTO.builder()
                    .username(user.getUsername())
                    .token(token)
                    .expirationTime("24h")
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), LOGIN_SUCCESS_MESSAGE, tokenDTO);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST,LOGIN_FAIL_MESSAGE, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<UserInfoResponseDTO> register(RegisterRequestDTO registerRequestDTO) {
        try {
            // Validate input data
            Validator.validateRegister(
                    registerRequestDTO.getUsername(),
                    registerRequestDTO.getPassword(),
                    registerRequestDTO.getRePassword(),
                    registerRequestDTO.getFullName(),
                    registerRequestDTO.getPhone(),
                    registerRequestDTO.getAddress(),
                    registerRequestDTO.getEmail());
            if (userService.existsByUsername(registerRequestDTO.getUsername())) {
                throw BusinessException.of(HttpStatus.CONFLICT, USERNAME_ALREADY_EXISTS_MESSAGE);
            }
            if (userService.exitsByEmail(registerRequestDTO.getEmail())) {
                throw BusinessException.of(HttpStatus.CONFLICT, EMAIL_ALREADY_EXISTS_MESSAGE);
            }
            if (userService.existsByPhoneNumber(registerRequestDTO.getPhone())) {
                throw BusinessException.of(HttpStatus.CONFLICT, PHONE_ALREADY_EXISTS_MESSAGE);
            }
            if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getRePassword())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PASSWORDS_DO_NOT_MATCH_MESSAGE);
            }

            // Ensure "CUSTOMER" role exists, otherwise create it
            Role userRole = roleRepository.findByRoleName("CUSTOMER")
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .roleName("CUSTOMER")
                                .deleted(false)
                                .build();
                        return roleRepository.save(newRole);
                    });

            // Create new user
            User user = User.builder()
                    .username(registerRequestDTO.getUsername())
                    .fullName(registerRequestDTO.getFullName())
                    .email(registerRequestDTO.getEmail().trim().toLowerCase())
                    .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                    .gender(registerRequestDTO.getGender())
                    .phone(registerRequestDTO.getPhone())
                    .address(registerRequestDTO.getAddress())
                    .deleted(false)
                    .emailConfirmed(false)
                    .build();

            User savedUser = userService.saveUser(user);

            // Assign role to user
            UserRole newUserRole = UserRole.builder()
                    .user(savedUser)
                    .role(userRole)
                    .deleted(false)
                    .build();

            userRoleRepository.save(newUserRole);

            // Send email confirmation
            Token token = emailConfirmationService.createEmailConfirmationToken(savedUser);
            try {
                emailConfirmationService.sendConfirmationEmail(savedUser, token);
            } catch (Exception e) {
                throw BusinessException.of(REGISTER_FAIL_MESSAGE);
            }

            UserInfoResponseDTO userResponseDTO = UserInfoResponseDTO.builder()
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .fullName(savedUser.getFullName())
                    .role(RoleName.CUSTOMER)
                    .build();

            return new GeneralResponse<>(HttpStatus.CREATED.value(), Constants.Message.EMAIL_CONFIRMATION_REQUEST_MESSAGE, userResponseDTO);
        } catch (BusinessException be){
            throw be;
        } catch (Exception e) {
            throw BusinessException.of(REGISTER_FAIL_MESSAGE);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<String> confirmEmail(String token) {
        try {
            Token emailToken = emailConfirmationService.validateConfirmationToken(token);
            User user = emailToken.getUser();
            user.setEmailConfirmed(true);
            userService.saveUser(user);
            return new GeneralResponse<>(HttpStatus.OK.value(), Constants.Message.EMAIL_CONFIRMED_SUCCESS_MESSAGE, null);
        } catch (Exception e) {
            throw BusinessException.of(CONFIRM_EMAIL_FAILED);
        }

    }

    @Override
    public GeneralResponse<List<Role>> getRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            return new GeneralResponse<>(HttpStatus.OK.value(), Constants.Message.ROLES_RETRIEVED_SUCCESS_MESSAGE, roles);
        } catch (Exception e) {
            throw BusinessException.of(ROLES_RETRIEVED_FAIL_MESSAGE);
        }
    }

}
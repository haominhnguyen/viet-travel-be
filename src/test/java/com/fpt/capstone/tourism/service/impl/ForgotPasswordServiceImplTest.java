package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.fpt.capstone.tourism.constants.Constants.Message.*;

class ForgotPasswordServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailConfirmationService emailConfirmationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ForgotPasswordServiceImpl forgotPasswordService;

    private User mockUser;
    private Token mockToken;
    private String mockEmail = "test@example.com";
    private String validPassword = "Password123!";
    private String invalidPassword = "123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setEmail(mockEmail);
        mockUser.setEmailConfirmed(true);
        mockUser.setPassword("encodedPassword");

        mockToken = new Token();
        mockToken.setToken("mockToken");
        mockToken.setUser(mockUser);
    }

    @Test
    void forgotPassword_Success() {
        when(userService.findUserByEmail(mockEmail)).thenReturn(mockUser);
        when(userService.exitsByEmail(mockEmail)).thenReturn(true);
        when(emailConfirmationService.createEmailConfirmationToken(mockUser)).thenReturn(mockToken);
        doNothing().when(emailConfirmationService).sendForgotPasswordEmail(mockUser, mockToken);

        GeneralResponse<String> response = forgotPasswordService.forgotPassword(mockEmail);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(RESET_PASSWORD_REQUEST_SUCCESS, response.getMessage());
        assertEquals("mockToken", response.getData());

        verify(userService).findUserByEmail(mockEmail);
        verify(emailConfirmationService).createEmailConfirmationToken(mockUser);
        verify(emailConfirmationService).sendForgotPasswordEmail(mockUser, mockToken);
    }

    @Test
    void forgotPassword_FailsWhenEmailDoesNotExist() {
        when(userService.exitsByEmail(mockEmail)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> forgotPasswordService.forgotPassword(mockEmail));

        assertEquals(EMAIL_INVALID, exception.getMessage());
    }

    @Test
    void forgotPassword_FailsWhenEmailNotConfirmed() {
        mockUser.setEmailConfirmed(false);
        when(userService.findUserByEmail(mockEmail)).thenReturn(mockUser);
        when(userService.exitsByEmail(mockEmail)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> forgotPasswordService.forgotPassword(mockEmail));

        assertEquals(EMAIL_INVALID, exception.getMessage());
    }

    @Test
    void resetPassword_Success() {
        when(emailConfirmationService.validateConfirmationToken("mockToken")).thenReturn(mockToken);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");

        GeneralResponse<String> response = forgotPasswordService.resetPassword(
                "mockToken", mockEmail, validPassword, validPassword);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(PASSWORD_UPDATED_SUCCESS_MESSAGE, response.getMessage());
        assertEquals(mockEmail, response.getData());

        verify(userService).saveUser(mockUser);
    }


    @Test
    void resetPassword_FailsWhenTokenInvalid() {
        when(emailConfirmationService.validateConfirmationToken("invalidToken"))
                .thenThrow(BusinessException.of(INVALID_CONFIRMATION_TOKEN_MESSAGE));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> forgotPasswordService.resetPassword("invalidToken", mockEmail, validPassword, validPassword));

        assertEquals(INVALID_CONFIRMATION_TOKEN_MESSAGE, exception.getMessage());
    }

    @Test
    void resetPassword_FailsWhenPasswordsDoNotMatch() {
        when(emailConfirmationService.validateConfirmationToken("mockToken")).thenReturn(mockToken);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> forgotPasswordService.resetPassword("mockToken", mockEmail, validPassword, "wrongPassword"));

        assertEquals(PASSWORDS_DO_NOT_MATCH_MESSAGE, exception.getMessage());
    }

    @Test
    void resetPassword_FailsWhenPasswordInvalid() {
        when(emailConfirmationService.validateConfirmationToken("mockToken")).thenReturn(mockToken);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> forgotPasswordService.resetPassword("mockToken", mockEmail, invalidPassword, invalidPassword));

        assertEquals(PASSWORD_INVALID, exception.getMessage());
    }

    @Test
    void generateToken_Success() {
        String token = forgotPasswordService.generateToken(mockEmail);
        assertNotNull(token);
    }
}


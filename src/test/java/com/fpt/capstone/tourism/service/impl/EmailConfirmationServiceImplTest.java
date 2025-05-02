package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.EmailConfirmationTokenRepository;
import com.fpt.capstone.tourism.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailConfirmationServiceImplTest {

    @Mock
    private EmailConfirmationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailConfirmationServiceImpl emailConfirmationService;

    private User user;
    private Token token;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("John Doe");
        user.setUsername("johndoe");

        token = new Token();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
    }

    @Test
    void createEmailConfirmationToken_Success() {
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        Token createdToken = emailConfirmationService.createEmailConfirmationToken(user);

        assertNotNull(createdToken);
        assertEquals(user, createdToken.getUser());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void sendConfirmationEmail_Success() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> emailConfirmationService.sendConfirmationEmail(user, token));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void validateConfirmationToken_Success() {
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        Token validatedToken = emailConfirmationService.validateConfirmationToken(token.getToken());

        assertNotNull(validatedToken);
        assertTrue(validatedToken.isUsed());
        verify(tokenRepository, times(1)).save(validatedToken);
    }

    @Test
    void validateConfirmationToken_ExpiredToken() {
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                emailConfirmationService.validateConfirmationToken(token.getToken()));
        assertEquals("Liên kết xác nhận không hợp lệ hoặc đã hết hạn", exception.getResponseMessage());
    }

    @Test
    void validateConfirmationToken_AlreadyUsedToken() {
        token.setUsed(true);
        when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                emailConfirmationService.validateConfirmationToken(token.getToken()));
        assertEquals("Email này đã được xác nhận trước đó. Không cần xác nhận lại", exception.getResponseMessage());
    }

    @Test
    void sendForgotPasswordEmail_Success() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> emailConfirmationService.sendForgotPasswordEmail(user, token));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendAccountServiceProvider_Success() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> emailConfirmationService.sendAccountServiceProvider(user, "randomPass123"));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}

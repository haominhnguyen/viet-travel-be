package com.fpt.capstone.tourism.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_Success() {
        assertDoesNotThrow(() -> emailService.sendEmail("test@example.com", "Subject", "Content"));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendEmail_Failure_MessagingException() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use `doThrow` instead of `when().thenThrow()`
        doThrow(new RuntimeException("Failed to send email")).when(mailSender).send(mimeMessage);

        assertThrows(RuntimeException.class, () ->
                emailService.sendEmail("test@example.com", "Subject", "Message Content")
        );

        verify(mailSender, times(1)).send(mimeMessage);
    }
}

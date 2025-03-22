package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.MailServiceDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.EmailConfirmationTokenRepository;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fpt.capstone.tourism.constants.Constants.Message.INVALID_CONFIRMATION_TOKEN_MESSAGE;
import static com.fpt.capstone.tourism.constants.Constants.Message.TOKEN_USED_MESSAGE;

@Service
@RequiredArgsConstructor
public class EmailConfirmationServiceImpl implements EmailConfirmationService {
    private final EmailConfirmationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    public Token createEmailConfirmationToken(User user) {
        Token token = new Token();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        return tokenRepository.save(token);
    }

    @Override
    public void sendConfirmationEmail(User user, Token token) {
        try {
            //Token encryptor when need
            //String encryptedToken = TokenEncryptorImpl.encrypt(token.getToken());

            String link = "http://localhost:8080/api/auth/confirm-email?token=" + token.getToken();
            String subject = "Viet Travel Email Confirmation";
            String content = "Dear " + user.getFullName() + ",\n\n"
                    + "Welcome to Viet Travel! We are thrilled to have you join our community."
                    + "\nWe hope you have fun and enjoy exploring Viet Nam with us.\n\n"
                    + "To confirm your email address, please click the link below:\n" + link;

            emailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            throw BusinessException.of(Constants.Message.TOKEN_ENCRYPTION_FAILED_MESSAGE, e);
        }
    }
    public String generateTemporaryToken() {
        return UUID.randomUUID().toString();
    }


    @Override
    public void sendForgotPasswordEmail(User user, Token token) {
        try {


            String link = "http://localhost:8080/api/reset-password?token=" + token.getToken();
            String subject = "Reset Password";
            String content = "Dear " + user.getFullName() + ",\n\n"
                    + "Hello,"
                    + "\nYou have requested to reset your password.\n\n"
                    + "Click the link below to change your password:\n" + link
                    +"\nIgnore this email if you do remember your password, or you have not made the request.";

            emailService.sendEmail(user.getEmail(), subject, content);

        } catch (Exception e) {
            throw BusinessException.of(Constants.Message.TOKEN_ENCRYPTION_FAILED_MESSAGE, e);
        }
    }



    @Override
    public Token validateConfirmationToken(String token) {
        Token emailToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> BusinessException.of(INVALID_CONFIRMATION_TOKEN_MESSAGE));

        if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw BusinessException.of(INVALID_CONFIRMATION_TOKEN_MESSAGE);
        }

        if (emailToken.isUsed()) {
            throw BusinessException.of(TOKEN_USED_MESSAGE);
        }

        emailToken.setUsed(true);
        tokenRepository.save(emailToken);

        return emailToken;
    }

    @Override
    public void sendAccountServiceProvider(User user, String randomPassword) {
        try {
            String subject = "Register to be Service Provide with Viet Travel";
            String content = "Dear " + user.getFullName() + ",\n\n"
                    + "Hello,"
                    + "\nYou have been become a service provider for Viet Travel.\n\n"
                    + "This is your account to access to web:\n"
                    + "Account: " + user.getUsername()
                    + "Password: " + randomPassword
                    +"\nPlease log in web to change password. If you have any problem please contact to Viet Travel via this email address.";

            emailService.sendEmail(user.getEmail(), subject, content);

        } catch (Exception e) {
            throw BusinessException.of("Send email account fail", e);
        }
    }

    @Override
    public void sendMailServiceProvider(MailServiceDTO mailServiceDTO) {
        try {
            emailService.sendEmail(mailServiceDTO.getProviderEmail(), mailServiceDTO.getEmailSubject(), mailServiceDTO.getEmailContent());

        } catch (Exception e) {
            throw BusinessException.of("Send email order service fail", e);
        }
    }

}

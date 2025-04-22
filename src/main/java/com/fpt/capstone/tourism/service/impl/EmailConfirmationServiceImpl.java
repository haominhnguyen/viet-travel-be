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

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

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

            String link = "https://traveltoday.cloud/confirm-email?token=" + token.getToken();
            String subject = "Xác Nhận Email Viet Travel";
            String content = "Kính gửi " + user.getFullName() + ",\n\n"
                    + "Chào mừng bạn đến với Viet Travel! Chúng tôi rất vui mừng khi bạn tham gia cộng đồng của chúng tôi."
                    + "\nChúng tôi hy vọng bạn sẽ có nhiều trải nghiệm thú vị khi khám phá Việt Nam cùng chúng tôi.\n\n"
                    + "Để xác nhận địa chỉ email của bạn, vui lòng nhấp vào liên kết dưới đây:\n" + link;

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
            String link = "https://traveltoday.cloud/api/v1/reset-password?token=" + token.getToken();
            String subject = "Đặt Lại Mật Khẩu";
            String content = "Kính gửi " + user.getFullName() + ",\n\n"
                    + "Xin chào,"
                    + "\nBạn đã yêu cầu đặt lại mật khẩu của mình.\n\n"
                    + "Vui lòng nhấp vào liên kết dưới đây để thay đổi mật khẩu của bạn:\n" + link
                    + "\nVui lòng bỏ qua email này nếu bạn vẫn nhớ mật khẩu của mình hoặc bạn không thực hiện yêu cầu này.";

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
            String subject = "Đăng Ký Làm Nhà Cung Cấp Dịch Vụ Với Viet Travel";
            String content = "Kính gửi " + user.getFullName() + ",\n\n"
                    + "Xin chào,"
                    + "\nBạn đã trở thành nhà cung cấp dịch vụ cho Viet Travel.\n\n"
                    + "Đây là tài khoản của bạn để truy cập vào hệ thống:\n"
                    + "Tài khoản: " + user.getUsername() + "\n"
                    + "Mật khẩu: " + randomPassword + "\n"
                    + "\nVui lòng đăng nhập vào hệ thống để thay đổi mật khẩu. Nếu bạn gặp bất kỳ vấn đề nào, vui lòng liên hệ với Viet Travel qua địa chỉ email này.";

            emailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            throw BusinessException.of(SEND_EMAIL_ACCOUNT_FAIL, e);
        }
    }

    @Override
    public void sendMailServiceProvider(MailServiceDTO mailServiceDTO) {
        try {
            emailService.sendEmail(mailServiceDTO.getProviderEmail(), mailServiceDTO.getEmailSubject(), mailServiceDTO.getEmailContent());
        } catch (Exception e) {
            throw BusinessException.of(SEND_EMAIL_ORDER_SERVICE_FAIL, e);
        }
    }
}

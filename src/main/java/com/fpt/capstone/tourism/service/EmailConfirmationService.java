package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.MailServiceDTO;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.User;

public interface EmailConfirmationService {

    void sendForgotPasswordEmail(User user, Token token);

    Token createEmailConfirmationToken(User user);
    void sendConfirmationEmail(User user, Token token) throws Exception;
    Token validateConfirmationToken(String token);

    void sendAccountServiceProvider(User user, String randomPassword);

    void sendMailServiceProvider(MailServiceDTO mailServiceDTO);
}

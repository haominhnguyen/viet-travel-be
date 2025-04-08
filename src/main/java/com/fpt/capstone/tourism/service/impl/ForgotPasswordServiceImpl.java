package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.model.Token;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.ForgotPasswordService;
import com.fpt.capstone.tourism.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final UserService userService;
    private final EmailConfirmationService emailConfirmationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public GeneralResponse<String> forgotPassword(String email) {
        try {
            User user = userService.findUserByEmail(email);

            //Check email valid or not
            if (!userService.exitsByEmail(email)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, EMAIL_INVALID);
            }
            //Check email is confirm or not
            if(!user.isEmailConfirmed()){
                throw BusinessException.of(HttpStatus.BAD_REQUEST, EMAIL_INVALID);
            }
            //Generate token
            Token token = emailConfirmationService.createEmailConfirmationToken(user);
            //Send email to reset password
            emailConfirmationService.sendForgotPasswordEmail(user, token);
            return new GeneralResponse<>(HttpStatus.OK.value(), RESET_PASSWORD_REQUEST_SUCCESS, token.getToken());
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(RESET_PASSWORD_REQUEST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<String> resetPassword(String token, String email, String newPassword, String newRePassword) {
        try {
            Token emailToken = emailConfirmationService.validateConfirmationToken(token);
            User user = emailToken.getUser();
            if (!email.equals(user.getEmail())) {
                throw BusinessException.of(HttpStatus.FORBIDDEN.toString());
            }
            //Check valid password
            if (!(Validator.isPasswordValid(newPassword))) {
                throw BusinessException.of(PASSWORD_INVALID);
            }
            if (!newPassword.equals(newRePassword)) {
                throw BusinessException.of(PASSWORDS_DO_NOT_MATCH_MESSAGE);
            }
            //Change password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            return new GeneralResponse<>(HttpStatus.OK.value(), PASSWORD_UPDATED_SUCCESS_MESSAGE, email);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(PASSWORD_UPDATED_FAIL_MESSAGE, ex);
        }

    }

    public String generateToken(String email) {
        Long currentTimes = System.currentTimeMillis();
        String tokenRaw = email + ":" + currentTimes;
        return Base64.getEncoder().encodeToString(tokenRaw.getBytes(StandardCharsets.UTF_8));
    }


}

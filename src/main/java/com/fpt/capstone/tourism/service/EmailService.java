package com.fpt.capstone.tourism.service;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendEmailHtml(String to, String subject, String content);
}

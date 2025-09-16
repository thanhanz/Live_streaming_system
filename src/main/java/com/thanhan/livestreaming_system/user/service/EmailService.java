package com.thanhan.livestreaming_system.user.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);
}

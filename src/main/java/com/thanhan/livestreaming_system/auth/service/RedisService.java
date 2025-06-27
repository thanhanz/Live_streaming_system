package com.thanhan.livestreaming_system.auth.service;

import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface RedisService {
    void addToBlackList(String token, String refreshToken) throws JOSEException, ParseException;
    boolean isInBlackList(String token);
}

package com.thanhan.livestreaming_system.membership.service;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.membership.dto.response.PaymentTransactionResponse;
import com.thanhan.livestreaming_system.membership.entity.PaymentTransaction;
import com.thanhan.livestreaming_system.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface PaymentService {
     String createPaymentUrl(HttpServletRequest request, User user);
     //Using IPN Url
     PaymentTransactionResponse handleCallbackPaymentHttps(Map<String, String> request, User user);
}

package com.thanhan.livestreaming_system.membership.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.membership.dto.response.PaymentTransactionResponse;
import com.thanhan.livestreaming_system.membership.service.PaymentService;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;
    UserService userService;

    @GetMapping("/create_payment")
    public ApiResponse createPayment(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);

        String responseUrl = paymentService.createPaymentUrl(request, user);
        return ApiResponse.builder()
                .message("Get payment Url success!")
                .data(responseUrl)
                .status(201)
                .build();
    }

    /*
    Handle Vnpay callback and update service
     */
    @GetMapping("/payment_callback")
    public ApiResponse<PaymentTransactionResponse> paymentCallback(@RequestParam Map<String, String> params) {
        PaymentTransactionResponse response = paymentService.handleCallbackPaymentHttps(params);

        return ApiResponse.<PaymentTransactionResponse>builder()
                .status(200)
                .message("Payment success!")
                .data(response)
                .build();

    }
}

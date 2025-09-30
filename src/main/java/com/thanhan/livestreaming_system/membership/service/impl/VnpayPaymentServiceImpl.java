package com.thanhan.livestreaming_system.membership.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.common.exception.VnpErrorCode;
import com.thanhan.livestreaming_system.common.exception.VnpPaymentException;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.configuration.VnpayConfig;
import com.thanhan.livestreaming_system.membership.dto.PaymentTransactionMapper;
import com.thanhan.livestreaming_system.membership.dto.response.PaymentTransactionResponse;
import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.entity.PaymentTransaction;
import com.thanhan.livestreaming_system.membership.entity.TransactionStatus;
import com.thanhan.livestreaming_system.membership.repository.PaymentTransactionRepository;
import com.thanhan.livestreaming_system.membership.service.MembershipPackageService;
import com.thanhan.livestreaming_system.membership.service.PaymentService;
import com.thanhan.livestreaming_system.membership.service.UserMembershipService;
import com.thanhan.livestreaming_system.membership.utils.VnpayUtil;
import com.thanhan.livestreaming_system.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnpayPaymentServiceImpl implements PaymentService {

    VnpayConfig vnpayConfig;
    UserMembershipService userMembershipService;
    MembershipPackageService membershipPackageService;
    PaymentTransactionRepository paymentTransactionRepository;

    private final String gateway = "VNPAY";

    @Override
    @Transactional
    public String createPaymentUrl(HttpServletRequest request, User user) {
        String mbsPackageId = request.getParameter("packageId");
        MembershipPackage mpk = membershipPackageService.getPackageById(Long.valueOf(mbsPackageId));

        long amount = mpk.getPrice() * 100L;

        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig(mpk.getId().toString(), user.getUsername(), mpk.getChannel().getId().toString());
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VnpayUtil.getIpAddress(request));

        String queryUrl = VnpayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VnpayUtil.getPaymentURL(vnpParamsMap, false);
        log.info("[Hash data]: " + hashData);
        String vnpSecureHash = VnpayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setGateway(gateway);
        transaction.setTransactionId(vnpParamsMap.get("vnp_TxnRef"));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setUser(user);
        transaction.setMembershipPackage(mpk);

        paymentTransactionRepository.save(transaction);
        return paymentUrl;
    }

    @Override
    @Transactional
    public PaymentTransactionResponse handleCallbackPaymentHttps(Map<String, String> request) {
        if (!verifyIpn(request)) {
            throw new VnpPaymentException(VnpErrorCode.SIGNATURE_FAILED);
        }
        String transactionId = request.get("vnp_TxnRef");
        PaymentTransaction transaction = paymentTransactionRepository.getTransactionByTransactionId(transactionId);

        if (transaction == null) {
            throw new VnpPaymentException(VnpErrorCode.TRANSACTION_NOT_FOUND);
        }

        if (transaction.getStatus().equals(TransactionStatus.SUCCESS)) {
            throw new RuntimeException("Transaction already success!");
        }


        String responseCode = request.get("vnp_ResponseCode");

        if (!"00".equals(responseCode)) {
            transaction.setStatus(TransactionStatus.FAILED);
            return PaymentTransactionMapper.toResponse(paymentTransactionRepository.save(transaction));
        } else {
            transaction.setStatus(TransactionStatus.SUCCESS);
            PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
            User user = savedTransaction.getUser();
            userMembershipService.createMembership(user, savedTransaction.getMembershipPackage());
            return PaymentTransactionMapper.toResponse(savedTransaction);
        }
    }

    private boolean verifyIpn(Map<String, String> params) {
        var reqSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String hashData = VnpayUtil.getPaymentURL(params, false);
        String secureHash = VnpayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);

        return reqSecureHash.equals(secureHash);
    }
}

package com.deardream.deardream_be.domain.payment.service;


import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.payment.SubscriptionRepository;
import com.deardream.deardream_be.domain.payment.entity.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.dto.KakaoReadyResponse;
import com.deardream.deardream_be.domain.payment.entity.Subscription;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.config.KakaoPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KakaoPayService {

    private final KakaoPayConfig kakaoPayConfig;
    private final FamilyRepository familyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "SECRET_KEY " + kakaoPayConfig.getSecretKey();
        headers.set("Authorization", auth);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    // 결제 완료 요청
    @Transactional
    public KakaoReadyResponse kakaoPayReady(Long userId, DeliveryType deliveryType) {
        // 가정일 경우만 결제 진행

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));


        Map<String, Object> parameters = new HashMap<>();

        String partnerOrderId = "order_" + userId + "_" + System.currentTimeMillis();

        // 결제 항목 구분
        String itemName;
        int totalAmount;

        if(deliveryType == DeliveryType.HOME) {
            itemName = "가정배송";
            totalAmount = 6900; // 가정배송의 경우 월 8900원 구독료
        } else if (deliveryType == DeliveryType.INSTITUTION) {
            itemName = "기관방문";
            totalAmount = 39000; // 기관방문의 경우 월 39000 구독료
        } else {
            throw new GeneralException(ErrorStatus._INVALID_DELIVERY_TYPE);
        }

        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("partner_order_id", partnerOrderId); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", userId); // 실제 사용자 ID로 교체
        parameters.put("item_name", itemName); // 실제 상품명으로 교체
        parameters.put("quantity", 1); // 실제 수량으로 교체
        parameters.put("total_amount", totalAmount); // 가정의 경우 월 8900원 구독료
        parameters.put("tax_free_amount", 0); // 면세 금액, 필요시 설정
        parameters.put("approval_url", "http://localhost:3000/subscribe/pay/complete"); // 결제 성공 후 리다이렉트 URL
        parameters.put("cancel_url", "http://localhost:3000/api/v1/test/payment/cancel"); // 결제 취소 후 리다이렉트 URL
        parameters.put("fail_url", "http://localhost:3000/api/v1/test/payment/fail"); // 결제 실패 후 리다이렉트 URL

        log.info("카카오페이 결제 준비 요청: {}", parameters);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(parameters, this.getHeaders());

        log.info("카카오페이 결제 준비 요청 헤더: {}", request.getHeaders());

        RestTemplate restTemplate = new RestTemplate();

        KakaoReadyResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                request,
                KakaoReadyResponse.class
        );

        Payment payment = Payment.builder()
                .partnerOrderId(partnerOrderId)
                .tid(response.getTid())
                .user(user)
                .deliveryType(deliveryType)
                .build();

        paymentRepository.save(payment);

        return response;
    }

    // 결제 완료 승인
    @Transactional
    public KakaoApproveResponse approveResponse (String tid, String pgToken) {

        Payment payment = paymentRepository.findByTid(tid);

        if (payment == null) {
            throw new GeneralException(ErrorStatus._PAYMENT_REQUEST_FAIL);
        }
        // 카카오 요청
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("tid", tid); // 실제 결제 고유 번호로 교체
        parameters.put("partner_order_id", payment.getPartnerOrderId()); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", String.valueOf(payment.getUser().getId())); // 실제 사용자 ID로 교체
        parameters.put("pg_token", pgToken); // 결제 승인 토큰

        // parameter headers
        HttpEntity<Map<String, String>> request = new HttpEntity<>(parameters, this.getHeaders());

        log.info("카카오페이 결제 승인 요청: {}", parameters);

        RestTemplate restTemplate = new RestTemplate();

        KakaoApproveResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/approve",
                request,
                KakaoApproveResponse.class
        );

        log.info("카카오페이 결제 승인 응답: {}", response);

        // 결제 성공 후 Subscription 갱신
        handlePaymentSuccess(payment, response);

        return response;
    }


    @Transactional
    protected void createPaymentForSubscription(Subscription subscription) {
        User user = subscription.getPayment().getUser();

        DeliveryType deliveryType = subscription.getPayment().getDeliveryType();

        // 결제 항목 구분
        String itemName;
        int totalAmount;

        if (deliveryType == DeliveryType.HOME) {
            itemName = "가정배송";
            totalAmount = 6900; // 가정배송의 경우 월 6900원 구독료
        } else if (deliveryType == DeliveryType.INSTITUTION) {
            itemName = "기관방문";
            totalAmount = 39000; // 기관방문의 경우 월 39000 구독료
        } else {
            throw new GeneralException(ErrorStatus._INVALID_DELIVERY_TYPE);
        }

        String newPartnerOrderId = "order_" + user.getId() + "_" + System.currentTimeMillis();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("sid", subscription.getPayment().getSid()); // 정기 결제 ID
        parameters.put("partner_order_id", newPartnerOrderId); // 새로운 주문 번호
        parameters.put("partner_user_id", user.getId()); // 사용자 ID
        parameters.put("item_name", itemName); // 상품 이름
        parameters.put("quantity", 1); // 수량
        parameters.put("total_amount", totalAmount); // 결제 금액
        parameters.put("tax_free_amount", 0); // 면세 금액 (필요시 설정)

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(parameters, this.getHeaders());
        log.info("카카오페이 정기 결제 승인 요청: {}", parameters);

        KakaoApproveResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/subscription",
                request,
                KakaoApproveResponse.class
        );

        log.info("카카오페이 정기 결제 승인 응답: {}", response);

        Payment payment = Payment.builder()
                .partnerOrderId(newPartnerOrderId)
                .tid(response.getTid())
                .user(user)
                .deliveryType(deliveryType)
                .build();

        paymentRepository.save(payment);

        subscription.extend(payment);

    }

    private void handlePaymentSuccess(Payment payment, KakaoApproveResponse response) {
        if(payment.getSubscription() == null) {
            Family family = Family.builder()
                    .leader(payment.getUser())
                    .build();

            familyRepository.save(family);

            Subscription subscription = new Subscription(payment, family);
            subscriptionRepository.save(subscription);

            payment.updateSubscription(subscription);
        } else {
            payment.getSubscription().extend(payment);
        }

        payment.updateSuccess(response.getSid());
        paymentRepository.save(payment);

    }

}

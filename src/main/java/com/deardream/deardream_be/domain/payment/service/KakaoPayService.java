package com.deardream.deardream_be.domain.payment.service;


import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.payment.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.dto.KakaoReadyResponse;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.config.KakaoPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleState;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KakaoPayService {

    private final KakaoPayConfig kakaoPayConfig;
    private final FamilyRepository familyRepository;
    private final PaymentRepository paymentRepository;
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
    public KakaoReadyResponse kakaoPayReady(Long familyId) {
        // 가정일 경우만 결제 진행

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        Map<String, Object> parameters = new HashMap<>();

        String partnerOrderId = "order_" + familyId + "_" + System.currentTimeMillis();

        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("partner_order_id", partnerOrderId); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", familyId); // 실제 사용자 ID로 교체
        parameters.put("item_name", "상품명"); // 실제 상품명으로 교체
        parameters.put("quantity", 1); // 실제 수량으로 교체
        parameters.put("total_amount", 8900); // 가정의 경우 월 8900원 구독료
        parameters.put("tax_free_amount", 0); // 면세 금액, 필요시 설정
        parameters.put("approval_url", "http://localhost:8080/api/v1/test/payment/success"); // 결제 성공 후 리다이렉트 URL
        parameters.put("cancel_url", "http://localhost:8080/api/v1/test/payment/cancel"); // 결제 취소 후 리다이렉트 URL
        parameters.put("fail_url", "http://localhost:8080/api/v1/test/payment/fail"); // 결제 실패 후 리다이렉트 URL

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
                .partnerUserId(String.valueOf(familyId)) // familyId를 String으로 변환
                .itemName("가정배송")
                .amountType(DeliveryType.HOME)
                .isActive(true)
                .family(family)
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
        parameters.put("partner_user_id", payment.getPartnerUserId()); // 실제 사용자 ID로 교체
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

        payment.setSid(response.getSid());
        payment.setApprovedAt(LocalDate.parse(response.getApproved_at()));
        paymentRepository.save(payment);

        return response;
    }

    @Transactional
    public KakaoApproveResponse subscriptionPayment(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        Payment payment = paymentRepository.findLastByFamily(family);

        if(payment.getSid() == null) {
            throw new PaymentException(PaymentErrorCode._PAYMENT_APPROVE_FAILED);
        }

        String newPartnerOrderId = "order_" + familyId + "_" + System.currentTimeMillis();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("sid", payment.getSid()); // 정기 결제 ID
        parameters.put("partner_order_id", newPartnerOrderId); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", payment.getPartnerUserId()); // 실제 사용자 ID로 교체
        parameters.put("item_name", payment.getItemName()); // 상품 이름
        parameters.put("quantity", 1); // 수량
        parameters.put("total_amount", 8900); // 결제 금액
        parameters.put("tax_free_amount", 0); // 면세 금액, 필요시 설정

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(parameters, this.getHeaders());

        KakaoApproveResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/subscription",
                request,
                KakaoApproveResponse.class
        );

        log.info("카카오페이 정기 결제 승인 응답: {}", response);

        // 신규 결제 저장
        Payment newPayment = Payment.builder()
                .partnerOrderId(newPartnerOrderId)
                .tid(response.getTid())
                .sid(response.getSid())
                .partnerUserId(payment.getPartnerUserId())
                .itemName(payment.getItemName())
                .amountType(DeliveryType.HOME)
                .approvedAt(LocalDate.parse(response.getApproved_at()))
                .isActive(true)
                .family(family)
                .build();

        paymentRepository.save(newPayment);

        log.info("새로운 정기 결제 저장: {}", newPayment);

        return response;

    }




}

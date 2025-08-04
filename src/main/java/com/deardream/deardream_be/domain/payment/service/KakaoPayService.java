package com.deardream.deardream_be.domain.payment.service;


import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.payment.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.PaymentStatus;
import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.dto.KakaoReadyResponse;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoCancelRequest;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoCancelSubscription;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoReadyRequestDto;
import com.deardream.deardream_be.domain.payment.dto.response.KakaoSubscriptionInactiveResponse;
import com.deardream.deardream_be.domain.payment.dto.response.KakaoSubscriptionResponse;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.domain.payment.util.KakaoPayRedirectUriList;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KakaoPayService {

    private static final int HOME_SUBSCRIPTION_AMOUNT = 8900;
    private static final String ITEM_NAME = "가정배송";

    private final KakaoPayConfig kakaoPayConfig;
    private final FamilyRepository familyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "SECRET_KEY " + kakaoPayConfig.getSecretKey();
        headers.set("Authorization", auth);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    // 결제 요청
    @Transactional
    public KakaoReadyResponse kakaoPayReady(KakaoReadyRequestDto request) {
        // 가정일 경우만 결제 진행

        String redirectUri = request.getRedirectUrl();

        if(!KakaoPayRedirectUriList.getAllowedRedirectUris().contains(redirectUri)) {
            log.warn("Invalid redirectUri received: {}", redirectUri);
            throw new GeneralException(ErrorStatus._INVALID_REDIRECT_URI);
        }

        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        if(!Objects.equals(family.getLeader().getId(), request.getOrderUserId()))
            throw new GeneralException(ErrorStatus._MUST_BE_LEADER_TO_DO);


        Map<String, Object> parameters = new HashMap<>();

        String partnerOrderId = "order_" + family.getId() + "_" + System.currentTimeMillis();

        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("partner_order_id", partnerOrderId); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", family.getId()); // 실제 사용자 ID로 교체
        parameters.put("item_name", ITEM_NAME); // 실제 상품명으로 교체
        parameters.put("quantity", 1); // 실제 수량으로 교체
        parameters.put("total_amount", HOME_SUBSCRIPTION_AMOUNT); // 가정의 경우 월 8900원 구독료
        parameters.put("tax_free_amount", 0); // 면세 금액, 필요시 설정
        parameters.put("approval_url", redirectUri + "/subscribe/pay/complete"); // 결제 성공 후 리다이렉트 URL
        parameters.put("cancel_url", redirectUri + "/subscribe/pay/cancel"); // 결제 취소 후 리다이렉트 URL
        parameters.put("fail_url", redirectUri + "/subscribe/pay/fail"); // 결제 실패 후 리다이렉트 URL

        log.info("카카오페이 결제 준비 요청: {}", parameters);

        HttpEntity<Map<String, Object>> kakaoRequest = new HttpEntity<>(parameters, this.getHeaders());

        log.info("카카오페이 결제 준비 요청 헤더: {}", kakaoRequest.getHeaders());

        RestTemplate restTemplate = new RestTemplate();

        KakaoReadyResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                kakaoRequest,
                KakaoReadyResponse.class
        );

        Payment payment = Payment.builder()
                .partnerOrderId(partnerOrderId)
                .tid(response.getTid())
                .familyId(family.getId()) // 연관 관계-가족
                .itemName(ITEM_NAME)
                .amountType(DeliveryType.HOME)
                .status(PaymentStatus.READY)
                .build();

        paymentRepository.save(payment);

        return response;
    }

    // 결제 완료 승인
    @Transactional
    public KakaoApproveResponse approveResponse (String tid, String pgToken) {

        Payment payment = paymentRepository.findByTid(tid)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode._PAYMENT_REQUEST_FAILED));

        if (payment.getSid() != null || payment.getApprovedAt() != null) {
            // 중복 결제
            paymentRepository.delete(payment);
            log.info("중복 결제 승인으로 삭제된 payment: tid={}", tid);
            throw new PaymentException(PaymentErrorCode._PAYMENT_ALREADY_APPROVED);
        }

        if (payment == null) {
            throw new GeneralException(ErrorStatus._PAYMENT_REQUEST_FAIL);
        }

        Family family = familyRepository.findById(payment.getFamilyId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        // 카카오 요청
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("tid", tid); // 실제 결제 고유 번호로 교체
        parameters.put("partner_order_id", payment.getPartnerOrderId()); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", String.valueOf(payment.getFamilyId())); // 실제 가족 ID로 교체
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

        payment.updateSuccess(response.getSid());

        //결제 완료 후 가족 활성화
        family.setFamilyActive();

        return response;
    }

    // 정기 결제
    @Transactional
    public KakaoSubscriptionResponse subscriptionPayment(String sid, Long familyId) {

        // 가족 정보 조회
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        // 정기 결제 번호 sid로 조회
        Payment payment = paymentRepository.findBySid(sid)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode._PAYMENT_REQUEST_FAILED));

        String newPartnerOrderId = "order_" + family.getId() + "_" + System.currentTimeMillis();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid()); // 가맹점 코드
        parameters.put("sid", payment.getSid()); // 정기 결제 ID
        parameters.put("partner_order_id", newPartnerOrderId); // 실제 주문 번호로 교체
        parameters.put("partner_user_id", family.getId()); // 실제 사용자 ID로 교체
        parameters.put("item_name", ITEM_NAME); // 상품 이름
        parameters.put("quantity", 1); // 수량
        parameters.put("total_amount", HOME_SUBSCRIPTION_AMOUNT); // 결제 금액
        parameters.put("tax_free_amount", 0); // 면세 금액, 필요시 설정

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(parameters, this.getHeaders());

        KakaoSubscriptionResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/subscription",
                request,
                KakaoSubscriptionResponse.class
        );

        log.info("카카오페이 정기 결제 승인 응답: {}", response);

        // 이전 결제 정보는 만료됨으로 수정
        payment.expiredPayment();

        // 신규 결제 저장
        Payment newPayment = Payment.builder()
                .partnerOrderId(response.getPartnerOrderId())
                .tid(response.getTid())
                .sid(response.getSid())
                .familyId(family.getId())
                .itemName(ITEM_NAME)
                .amountType(DeliveryType.HOME)
                .build();

        paymentRepository.save(newPayment);

        payment.updateSuccess(response.getSid());

        log.info("새로운 정기 결제 저장: {}", newPayment);

        return response;

    }

    // 환불
    @Transactional
    public void refundPayment(KakaoCancelRequest request) {
        // 결제 고유 번호로 주문 내역 찾기
        Payment payment = paymentRepository.findByTid(request.getTid())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode._PAYMENT_REQUEST_FAILED));

        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        // 결제 내역과 사용자 정보 조회
        if(!Objects.equals(family.getLeader().getId(), request.getOrderUserId()))
            throw new GeneralException(ErrorStatus._MUST_BE_LEADER_TO_DO);

        // 환불은 결제 후 3일 이내에만 가능합니다.
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if(payment.getApprovedAt().plusDays(3).isBefore(now))
            throw new PaymentException(PaymentErrorCode._PAYMENT_PERIOD_EXPIRED);

        // 카카오페이 정기 결제 취소 요청
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid());
        parameters.put("sid", payment.getSid());

        HttpEntity<Map<String, String>> kakaoRequest = new HttpEntity<>(parameters, this.getHeaders());

        restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/subscription/cancel",
                kakaoRequest,
                Void.class
        );

        // 결제 상태 변경
        payment.cancelPayment();

        // 가족 비활성화
        family.setFamilyDeActive();
    }

    // 정기 결제 비활성화
    @Transactional
    public void inactiveSubscription(KakaoCancelSubscription request) {
        // 정기 결제 상태는 INACTIVE
        // 가족은 여전히 isActive
        // 하지만, expiredAt 이후는 자동으로 비활성화 처리됨

        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        if(!Objects.equals(family.getLeader().getId(), request.getOrderUserId()))
            throw new GeneralException(ErrorStatus._MUST_BE_LEADER_TO_DO);

        // 정기 결제 번호로 조회
        Payment payment = paymentRepository.findTopByFamilyIdAndStatusOrderByApprovedAtDesc(family.getId(), PaymentStatus.ACTIVE)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode._PAYMENT_REQUEST_FAILED));

        if(payment.getSid() == null)
            throw new PaymentException(PaymentErrorCode._INVALID_PAYMENT_REQUEST);

        // 카카오페이 정기 결제 비활성화 요청
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", kakaoPayConfig.getCid());
        parameters.put("sid", payment.getSid());

        HttpEntity<Map<String, Object>> inactiveResponse = new HttpEntity<>(parameters, this.getHeaders());
        KakaoSubscriptionInactiveResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/manage/subscription/inactive",
                inactiveResponse,
                KakaoSubscriptionInactiveResponse.class
        );

        // 정기 구독이 해지 됩니다.
        payment.inactivePayment();

        // 구독 해지와 함께 글 작성 등이 불가능합니다.
        family.setFamilyDeActive();

    }
}

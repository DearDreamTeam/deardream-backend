package com.deardream.deardream_be.domain.payment.service;

import com.deardream.deardream_be.domain.payment.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    /*
    결제 성공 시 Payment 저장
    해당 유저가 이미 Subscription 을 가지고 있다면 갱신
    없다면 새로 생성
    상태는 ACTIVE, 1개월 후 만료
     */
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void createOrUpdateSubscription(){}

}

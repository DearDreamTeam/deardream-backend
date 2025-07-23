package com.deardream.deardream_be.domain.payment.entity;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscription")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;

    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    // 대표자가 결제를 진행합니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 대표자(결제한 사용자)의 가족
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false, unique = true)
    private Family family;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_payment_id")
    private Payment payment;

    public boolean isValid() {
        return status == SubscriptionStatus.ACTIVE && LocalDateTime.now().isBefore(expiredAt);
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCLE;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }





}

package com.deardream.deardream_be.domain.payment.entity;

import com.deardream.deardream_be.domain.family.entity.Family;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

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

    @NotNull
    private LocalDateTime startedAt;

    @NotNull
    private LocalDateTime expiredAt;

    @NotNull
    private SubscriptionStatus status; // ACTIVE, INACTIVE, CANCELLED

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;


    public Subscription(Payment payment, Family family) {
        this.payment = payment;
        this.family = family;
        this.startedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.expiredAt = startedAt.plusMonths(1); // 1개월 후 만료
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void active() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void extend(Payment payment) {
        this.payment = payment;
        this.expiredAt = expiredAt.plusMonths(1); // 1개월 연장
        this.status = SubscriptionStatus.ACTIVE;
    }


}

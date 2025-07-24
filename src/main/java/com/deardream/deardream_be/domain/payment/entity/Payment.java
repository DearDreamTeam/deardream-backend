package com.deardream.deardream_be.domain.payment.entity;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment extends BaseEntity {

    // 가맹점 주문 고유 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String partnerOrderId;

    // 결제 고유 번호, 결제 준비 API 응답에 포함
    @NotNull
    private String tid;

    // 정기 결제용 ID, 정기 결제의 경우에만 존재
    @Setter
    private String sid;

    // 가맹점 회원 ID, 결제 준비 API 응답과 일치
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // 자택 배송과 기관 방문 중 선택
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType deliveryType;

    private LocalDateTime approvedAt;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private Subscription subscription;


    public void updateSuccess(String sid) {
        this.sid = sid;
        this.approvedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    // 결제 취소 시 처리
    public void cancel() {
        if(this.subscription != null) {
            this.subscription.cancel();
        }
    }

    public void updateSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

}

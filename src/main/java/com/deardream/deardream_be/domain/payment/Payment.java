package com.deardream.deardream_be.domain.payment;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.cglib.core.Local;
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

    @JoinColumn(name = "family_id", nullable = false)
    private Long familyId;

    @NotNull
    private String itemName;

    // 자택 배송과 기관 방문 중 선택
    @Enumerated(EnumType.STRING)
    private DeliveryType amountType;

    private LocalDateTime approvedAt;

    private LocalDateTime expiredAt;

    // 결제 상태 (준비, 완료, 취소 등)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public void updateSuccess(String sid) {
        this.sid = sid;
        this.approvedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.expiredAt = approvedAt.plusMonths(1);
        this.status = PaymentStatus.ACTIVE;
    }

    public void cancelPayment() {
        this.status = PaymentStatus.CANCELLED;
        this.expiredAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void inactivePayment() {
        this.status = PaymentStatus.INACTIVE;
        this.expiredAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void expiredPayment() {
        this.status = PaymentStatus.EXPIRED;
    }
}

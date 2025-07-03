package com.deardream.deardream_be.domain.payment;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @NotNull
    private String partnerUserId;

    @NotNull
    private String itemName;

    @ManyToOne
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    // 자택 배송과 기관 방문 중 선택
    @Enumerated(EnumType.STRING)
    private DeliveryType amountType;

    @Setter
    private LocalDate approvedAt;

    private Boolean isActive;


}

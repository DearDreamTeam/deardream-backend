package com.deardream.deardream_be.domain.payment;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    // 자택 배송과 기관 방문 중 선택
    @Enumerated(EnumType.STRING)
    private DeliveryType amountType;

    private Boolean isActive;

}

package com.deardream.deardream_be.domain.payment;

public enum PaymentStatus {
    READY,       // 결제 준비 상태
    ACTIVE, // 정기 결제 활성화 상태
    INACTIVE, // 정기 결제 비활성화 상태
    EXPIRED,    // 정기 결제 만료 상태
    CANCELLED,   // 결제 취소 상태
    FAILED;      // 결제 실패 상태

}

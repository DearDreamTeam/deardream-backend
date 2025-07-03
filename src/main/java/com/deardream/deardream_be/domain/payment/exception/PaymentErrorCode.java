package com.deardream.deardream_be.domain.payment.exception;

import com.deardream.deardream_be.global.apiPayload.code.BaseErrorCode;
import com.deardream.deardream_be.global.apiPayload.code.errorDto.ErrorReasonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {
    _PAYMENT_APPROVE_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제 승인에 실패하였습니다."),
    _INVALID_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "P002", "유효하지 않은 결제 요청입니다."),
    _PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "P003", "이미 완료된 결제입니다."),
    _PAYMENT_CANCELLED(HttpStatus.BAD_REQUEST, "P004", "결제가 취소되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .code(code)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}

package com.deardream.deardream_be.domain.payment.exception;

import com.deardream.deardream_be.global.apiPayload.code.BaseErrorCode;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;

public class PaymentException extends GeneralException {

    public PaymentException(BaseErrorCode code) {
        super(code);
    }
}

package com.deardream.deardream_be.global.apiPayload.code;


import com.deardream.deardream_be.global.apiPayload.code.errorDto.ErrorReasonDTO;

public interface BaseErrorCode {

    public ErrorReasonDTO getReason();

    public ErrorReasonDTO getReasonHttpStatus();
}

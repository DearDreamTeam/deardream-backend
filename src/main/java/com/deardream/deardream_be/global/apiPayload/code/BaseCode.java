package com.deardream.deardream_be.global.apiPayload.code;


import com.deardream.deardream_be.global.apiPayload.code.errorDto.ReasonDTO;

public interface BaseCode {
    public ReasonDTO getReason();

    public ReasonDTO getReasonHttpStatus();
}

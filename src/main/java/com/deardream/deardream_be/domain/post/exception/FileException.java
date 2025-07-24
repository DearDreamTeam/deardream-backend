package com.deardream.deardream_be.domain.post.exception;

import com.deardream.deardream_be.global.apiPayload.code.BaseErrorCode;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;

public class FileException extends GeneralException {
    public FileException(BaseErrorCode code) {super(code);}
}

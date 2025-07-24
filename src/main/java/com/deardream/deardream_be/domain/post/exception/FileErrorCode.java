package com.deardream.deardream_be.domain.post.exception;

import com.deardream.deardream_be.global.apiPayload.code.BaseErrorCode;
import com.deardream.deardream_be.global.apiPayload.code.errorDto.ErrorReasonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum FileErrorCode implements BaseErrorCode {
    _FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "파일을 찾을 수 없습니다."),
    _FILE_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "F002", "허용되지 않는 파일 확장자입니다."),
    _FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 업로드에 실패하였습니다."),
    _FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F004", "파일 삭제에 실패하였습니다."),
    _FILE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "F005", "이미 동일한 이름의 파일이 존재합니다."),
    _FILE_SIZE_TOO_LARGE(HttpStatus.BAD_REQUEST, "F006", "파일 크기가 너무 큽니다."),
    _FILE_EMPTY(HttpStatus.BAD_REQUEST, "F007", "업로드된 파일이 비어 있습니다."),
    _FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F008", "파일 읽기 중 오류가 발생했습니다."),
    _FILE_WRITE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F009", "파일 쓰기 중 오류가 발생했습니다."),
    _FILE_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "F010", "파일 형식이 올바르지 않습니다."),
    _FILE_UPLOAD_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "F011", "파일 업로드 시간이 초과되었습니다."),
    _FILE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "F012", "지원되지 않는 파일 형식입니다."),
    _FILE_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F013", "파일 변환에 실패했습니다."),
    _FILE_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F014", "파일 URL 생성에 실패하였습니다."),
    _FILE_CANNOT_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "F015", "파일을 삭제할 수 없습니다.");

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

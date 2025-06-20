// com.deardream.deardream_be.global.common.ApiResponse.java
package com.deardream.deardream_be.global.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;

    private ApiResponse(T data) {
        this.success = true;
        this.data = data;
    }

    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(data);
    }

}

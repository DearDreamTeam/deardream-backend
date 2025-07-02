package com.deardream.deardream_be.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadResult {
    private final String key;
    private final String url;
}

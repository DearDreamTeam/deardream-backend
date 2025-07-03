package com.deardream.deardream_be.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KakaoPayConfig {

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;
}

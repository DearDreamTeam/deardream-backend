package com.deardream.deardream_be.global.common;


import jakarta.annotation.PostConstruct;

import java.util.TimeZone;

public class JvmTimeZone {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}

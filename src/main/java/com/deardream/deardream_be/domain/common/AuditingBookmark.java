package com.deardream.deardream_be.domain.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditingBookmark {
    String value();
}

package com.deardream.deardream_be.domain.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditingAop {

    @Before("@annotation(auditingBookmark)")
    public void auditingBefore(JoinPoint point, AuditingBookmark auditingBookmark) {
        String action = auditingBookmark.value();
        String username = getCurrentUsername();

        log.info("Action: {}, User: {}, Method: {}",
                action,
                username,
                point.getSignature().toShortString()
        );
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
        } catch (Exception e) {
            return "Anonymous";
        }
    }
}

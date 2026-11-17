package com.openjar.notificationservice.timing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Times every business operation (com.openjar.notificationservice.service..*, which includes the RabbitMQ listeners)
 * and every database call made through a Spring Data repository.
 * Tune with logging.level.openjar.timing.op / openjar.timing.db (INFO, DEBUG or OFF).
 */
@Aspect
@Component
public class OperationTimingAspect {

    private static final Logger ops = LoggerFactory.getLogger("openjar.timing.op");
    private static final Logger db = LoggerFactory.getLogger("openjar.timing.db");

    private final long slowMs;

    public OperationTimingAspect(@Value("${openjar.timing.slow-ms:1000}") long slowMs) {
        this.slowMs = slowMs;
    }

    @Around("within(com.openjar.notificationservice.service..*) && execution(public * *(..))")
    public Object timeOperation(ProceedingJoinPoint pjp) throws Throwable {
        return time(pjp, ops, "op", pjp.getSignature().getDeclaringType().getSimpleName());
    }

    @Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
    public Object timeDatabase(ProceedingJoinPoint pjp) throws Throwable {
        // Report the application's repository interface, not Spring's CrudRepository/JpaRepository.
        String repo = Arrays.stream(pjp.getThis().getClass().getInterfaces())
                .filter(i -> i.getName().startsWith("com.openjar"))
                .map(Class::getSimpleName)
                .findFirst()
                .orElse(pjp.getSignature().getDeclaringType().getSimpleName());
        return time(pjp, db, "db", repo);
    }

    private Object time(ProceedingJoinPoint pjp, Logger logger, String kind, String owner) throws Throwable {
        if (!logger.isInfoEnabled()) {
            return pjp.proceed();
        }
        long start = System.nanoTime();
        boolean ok = false;
        try {
            Object result = pjp.proceed();
            ok = true;
            return result;
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            String line = String.format("%s %s.%s %d ms%s", kind, owner, pjp.getSignature().getName(), ms, ok ? "" : " (failed)");
            if (ms >= slowMs) logger.warn("{}  SLOW", line);
            else logger.info(line);
        }
    }
}

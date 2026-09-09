package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* org.example..*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        String className =
                joinPoint.getSignature().getDeclaringType().getSimpleName();

        String methodName =
                joinPoint.getSignature().getName();

        log.info("→ {}.{}() args={}",
                className,
                methodName,
                joinPoint.getArgs());

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;

            log.info("← {}.{}() result={} ({} ms)",
                    className,
                    methodName,
                    result,
                    duration);

            return result;

        } catch (Exception e) {

            log.error("✖ {}.{}() fehlgeschlagen: {}",
                    className,
                    methodName,
                    e.getMessage(),
                    e);

            throw e;
        }
    }
}
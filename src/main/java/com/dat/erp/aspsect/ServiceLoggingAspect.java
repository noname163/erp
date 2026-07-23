package com.dat.erp.aspsect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = resolveTargetClass(joinPoint);
        Logger logger = LoggerFactory.getLogger(targetClass);
        String className = targetClass.getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startedAt = System.currentTimeMillis();

        logger.info("SERVICE_CALL action=STARTED service={} method={}", className, methodName);
        try {
            Object result = joinPoint.proceed();
            logger.info("SERVICE_CALL action=COMPLETED service={} method={} status=SUCCESS executionTimeMs={}",
                    className, methodName, System.currentTimeMillis() - startedAt);
            return result;
        } catch (Throwable ex) {
            logger.error("SERVICE_CALL action=FAILED service={} method={} status=FAILED executionTimeMs={} errorType={} errorMessage={}",
                    className,
                    methodName,
                    System.currentTimeMillis() - startedAt,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        }
    }

    private Class<?> resolveTargetClass(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        return target == null ? joinPoint.getSignature().getDeclaringType() : AopUtils.getTargetClass(target);
    }
}

package com.dat.erp.systemconfigs;

import org.springframework.core.task.TaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        return createTaskExecutor("email-");
    }

    @Bean(name = "defaultSetupTaskExecutor")
    public Executor defaultSetupTaskExecutor() {
        return createTaskExecutor("setup-");
    }

    @Bean(name = "payrollCalculationTaskExecutor")
    public Executor payrollCalculationTaskExecutor() {
        return createTaskExecutor("payroll-calc-");
    }

    private Executor createTaskExecutor(String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(securityContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    private TaskDecorator securityContextTaskDecorator() {
        return runnable -> {
            SecurityContext callerContext = SecurityContextHolder.createEmptyContext();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            callerContext.setAuthentication(authentication);

            return () -> {
                SecurityContext previousContext = SecurityContextHolder.getContext();
                try {
                    SecurityContextHolder.setContext(callerContext);
                    runnable.run();
                } finally {
                    SecurityContextHolder.setContext(previousContext);
                }
            };
        };
    }
}


package com.dat.erp.systemconfigs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dat.erp.contexts.TenantContext;

class AsyncConfigTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void payrollCalculationTaskExecutor_propagatesSecurityAndTenantContexts() throws InterruptedException {
        AsyncConfig asyncConfig = new AsyncConfig();
        Executor executor = asyncConfig.payrollCalculationTaskExecutor();
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken("ACC-1", null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            TenantContext.setCompanyCode("COMPANY-1");

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Authentication> authenticationFromWorker = new AtomicReference<>();
            AtomicReference<String> companyCodeFromWorker = new AtomicReference<>();

            executor.execute(() -> {
                authenticationFromWorker.set(SecurityContextHolder.getContext().getAuthentication());
                companyCodeFromWorker.set(TenantContext.requireCompanyCode());
                latch.countDown();
            });

            assertEquals(true, latch.await(5, TimeUnit.SECONDS));
            assertNotNull(authenticationFromWorker.get());
            assertEquals("ACC-1", authenticationFromWorker.get().getPrincipal());
            assertEquals("COMPANY-1", companyCodeFromWorker.get());
        } finally {
            taskExecutor.shutdown();
        }
    }
}

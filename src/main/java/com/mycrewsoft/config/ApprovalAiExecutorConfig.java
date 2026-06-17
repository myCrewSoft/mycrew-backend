package com.mycrewsoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class ApprovalAiExecutorConfig {

    @Bean("approvalAiTaskExecutor")
    public ThreadPoolTaskExecutor approvalAiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("approval-ai-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setTaskDecorator(runnable -> {
            SecurityContext capturedContext = SecurityContextHolder.getContext();
            return () -> {
                SecurityContext previousContext = SecurityContextHolder.getContext();
                try {
                    SecurityContextHolder.setContext(capturedContext);
                    runnable.run();
                } finally {
                    SecurityContextHolder.setContext(previousContext);
                }
            };
        });
        return executor;
    }
}

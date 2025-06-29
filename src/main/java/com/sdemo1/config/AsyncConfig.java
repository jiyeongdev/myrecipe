package com.sdemo1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 레시피 추천 전용 스레드 풀
     * - corePoolSize: 기본 스레드 수 (항상 유지)
     * - maxPoolSize: 최대 스레드 수 (부하 시 증가)
     * - queueCapacity: 작업 큐 크기 (FIFO 처리)
     * - keepAliveTime: 유휴 스레드 유지 시간
     */
    @Bean(name = "recipeRecommendationExecutor")
    public Executor recipeRecommendationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 기본 스레드 개수 (항상 활성화)
        executor.setCorePoolSize(3);
        
        // 최대 스레드 개수 (부하 시 확장)
        executor.setMaxPoolSize(10);
        
        // 작업 큐 크기 (FIFO 방식으로 대기)
        executor.setQueueCapacity(50);
        
        // 스레드 이름 prefix
        executor.setThreadNamePrefix("RecipeRecommend-");
        
        // 유휴 스레드 유지 시간 (초)
        executor.setKeepAliveSeconds(60);
        
        // 애플리케이션 종료 시 진행 중인 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 거부된 작업 처리 정책 (호출자 스레드에서 실행)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }

    /**
     * 알림 전송 전용 스레드 풀 (가벼운 작업용)
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Notification-");
        executor.setKeepAliveSeconds(30);
        
        executor.initialize();
        return executor;
    }
} 
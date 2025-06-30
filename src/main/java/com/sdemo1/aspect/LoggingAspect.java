package com.sdemo1.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

/**
 * AOP 로깅 및 성능 모니터링
 * - 메서드 실행 시간 측정
 * - 요청/응답 로깅
 * - 예외 상황 기록
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * Service 레이어의 모든 메서드에 대한 포인트컷
     */
    @Pointcut("execution(* com.sdemo1.service.*.*(..))")
    public void serviceLayer() {}

    /**
     * Controller 레이어의 모든 메서드에 대한 포인트컷
     */
    @Pointcut("execution(* com.sdemo1.controller.*.*(..))")
    public void controllerLayer() {}

    /**
     * 비동기 메서드에 대한 포인트컷
     */
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Async)")
    public void asyncMethods() {}

    /**
     * Service 레이어 메서드 실행 전후 로깅 및 성능 측정
     */
    @Around("serviceLayer()")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        try {
            // 메서드 시작 로그
            log.info("[{}] {}.{}() 시작 - 파라미터: {}",
                    Thread.currentThread().getName(),
                    className,
                    methodName,
                    formatArgs(args));

            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();

            // 메서드 완료 로그
            log.info("[{}] {}.{}() 완료 - 실행시간: {}ms",
                    Thread.currentThread().getName(),
                    className,
                    methodName,
                    stopWatch.getTotalTimeMillis());

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            
            // 예외 발생 로그
            log.error("[{}] {}.{}() 실패 - 실행시간: {}ms, 예외: {}",
                    Thread.currentThread().getName(),
                    className,
                    methodName,
                    stopWatch.getTotalTimeMillis(),
                    e.getMessage());
            
            throw e;
        }
    }

    /**
     * Controller 레이어 메서드 실행 로깅
     */
    @Around("controllerLayer()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            log.info("[API] {}.{}() 호출", className, methodName);
            
            Object result = joinPoint.proceed();
            
            log.info("[API] {}.{}() 응답 완료", className, methodName);
            
            return result;
            
        } catch (Exception e) {
            log.error("[API] {}.{}() 오류 발생: {}", className, methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * 비동기 메서드 실행 로깅
     */
    @Around("asyncMethods()")
    public Object logAsyncExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String threadName = Thread.currentThread().getName();
        
        try {
            log.info("[ASYNC-{}] {}.{}() 비동기 작업 시작",
                    threadName, className, methodName);
            
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            Object result = joinPoint.proceed();
            
            stopWatch.stop();
            log.info("[ASYNC-{}] {}.{}() 비동기 작업 완료 - 실행시간: {}ms",
                    threadName, className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
            
        } catch (Exception e) {
            log.error("[ASYNC-{}] {}.{}() 비동기 작업 실패: {}",
                    threadName, className, methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * 트랜잭션 메서드 실행 후 로깅
     */
    @AfterReturning(pointcut = "serviceLayer() && @annotation(org.springframework.transaction.annotation.Transactional)", 
                   returning = "result")
    public void logTransactionSuccess(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("[TXN] {}.{}() 트랜잭션 성공적으로 커밋", className, methodName);
    }

    /**
     * 트랜잭션 메서드에서 예외 발생 시 로깅
     */
    @AfterThrowing(pointcut = "serviceLayer() && @annotation(org.springframework.transaction.annotation.Transactional)", 
                  throwing = "exception")
    public void logTransactionFailure(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.error("[TXN] {}.{}() 트랜잭션 롤백 - 예외: {}",
                className, methodName, exception.getMessage());
    }

    /**
     * 파라미터를 로그에 적합한 형태로 포맷팅
     * (민감한 정보는 마스킹)
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "없음";
        }
        
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    } else if (arg instanceof String && arg.toString().length() > 100) {
                        return arg.toString().substring(0, 100) + "...";
                    } else {
                        return arg.toString();
                    }
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("없음");
    }
} 
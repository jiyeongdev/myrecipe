package com.sdemo1.exception;

import com.sdemo1.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러
 * - JSON 파싱 오류
 * - 인증/인가 오류
 * - 매개변수 타입 오류
 * - 비즈니스 로직 오류
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * JSON 파싱 오류 처리
     * @RequestBody에서 JSON 역직렬화 실패 시 발생
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleJsonParseError(HttpMessageNotReadableException e) {
        log.error("JSON 파싱 오류 발생: {}", e.getMessage());
        
        String message = "요청 데이터 형식이 올바르지 않습니다";
        
        // 더 구체적인 오류 메시지 제공
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Cannot deserialize")) {
                message = "JSON 데이터 형식이 잘못되었습니다";
            } else if (e.getMessage().contains("Required request body is missing")) {
                message = "요청 본문이 누락되었습니다";
            } else if (e.getMessage().contains("JSON parse error")) {
                message = "JSON 구문 오류입니다";
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(message, null, HttpStatus.BAD_REQUEST));
    }

    /**
     * 매개변수 타입 오류 처리
     * URL 파라미터나 Path Variable의 타입이 맞지 않을 때
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("매개변수 타입 오류 발생: 파라미터 '{}', 값 '{}', 기대 타입 '{}'", 
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName());
        
        String message = String.format("매개변수 '%s'의 값 '%s'이(가) 올바른 형식이 아닙니다", 
                e.getName(), e.getValue());
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(message, null, HttpStatus.BAD_REQUEST));
    }

    /**
     * Validation 오류 처리
     * @Valid 어노테이션으로 검증 실패 시
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationError(MethodArgumentNotValidException e) {
        log.error("검증 오류 발생: {}", e.getMessage());
        
        String message = "입력 데이터 검증에 실패했습니다";
        
        // 첫 번째 오류 메시지만 반환
        if (e.getBindingResult().hasErrors()) {
            message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(message, null, HttpStatus.BAD_REQUEST));
    }

    /**
     * 인증 오류 처리
     * JWT 토큰이 없거나 잘못된 경우
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationError(AuthenticationException e) {
        log.error("인증 오류 발생: {}", e.getMessage());
        
        String message = "인증이 필요합니다";
        
        if (e instanceof BadCredentialsException) {
            message = "인증 정보가 올바르지 않습니다";
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(message, null, HttpStatus.UNAUTHORIZED));
    }

    /**
     * 권한 오류 처리
     * 접근 권한이 없는 경우
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDenied(AccessDeniedException e) {
        log.error("권한 오류 발생: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("접근 권한이 없습니다", null, HttpStatus.FORBIDDEN));
    }

    /**
     * 비즈니스 로직 예외 처리
     * CustomException 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException e) {
        log.warn("비즈니스 로직 오류: {}", e.getMessage());
        
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode());
        
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(e.getMessage(), null, status));
    }

    /**
     * 숫자 형식 오류 처리
     * JWT 토큰에서 사용자 ID 파싱 실패 등
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<String>> handleNumberFormatError(NumberFormatException e) {
        log.error("숫자 형식 오류 발생: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("숫자 형식이 올바르지 않습니다", null, HttpStatus.BAD_REQUEST));
    }

    /**
     * 보안 예외 처리
     * 커스텀 SecurityException 처리
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<String>> handleSecurityException(SecurityException e) {
        log.error("보안 오류 발생: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.UNAUTHORIZED));
    }

    /**
     * 기타 예상하지 못한 오류 처리
     * 모든 예외의 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericError(Exception e) {
        log.error("예상하지 못한 오류 발생", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("서버 내부 오류가 발생했습니다", null, HttpStatus.INTERNAL_SERVER_ERROR));
    }
} 
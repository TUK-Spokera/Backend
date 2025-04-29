package graduation.spokera.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외처리기
 * throw 했을때 (예외 발생했을때) 클라이언트한테 메세지 보내는거
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 비즈니스용 예외 처리 (예: IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 2. 리소스 못 찾았을 때 (JPA 등에서 EntityNotFoundException이 발생 가능)
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(jakarta.persistence.EntityNotFoundException e) {
        return buildErrorResponse("리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    // 3. 예상하지 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        e.printStackTrace();
        return buildErrorResponse("서버 내부 오류가 발생했습니다. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 🔧 공통 응답 생성 메서드
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("success", false);
        errorBody.put("message", message);
        return ResponseEntity.status(status).body(errorBody);
    }
}
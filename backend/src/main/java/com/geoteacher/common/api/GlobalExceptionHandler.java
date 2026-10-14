package com.geoteacher.common.api;

import com.geoteacher.auth.application.AccountException;
import com.geoteacher.classroom.application.ClassroomErrorType;
import com.geoteacher.classroom.application.ClassroomException;
import com.geoteacher.question.application.QuestionException;
import com.geoteacher.document.application.DocumentException;
import com.geoteacher.ai.application.AiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiErrorResponse> handleAuthentication() {
        return error(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Tên đăng nhập hoặc mật khẩu không chính xác.",
                Map.of()
        );
    }

    @ExceptionHandler(AccountException.class)
    ResponseEntity<ApiErrorResponse> handleAccount(AccountException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ClassroomException.class)
    ResponseEntity<ApiErrorResponse> handleClassroom(ClassroomException exception) {
        var status = switch (exception.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INVALID_STATE -> HttpStatus.BAD_REQUEST;
        };
        return error(status, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(QuestionException.class)
    ResponseEntity<ApiErrorResponse> handleQuestion(QuestionException exception) {
        var status = switch (exception.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INVALID_STATE -> HttpStatus.BAD_REQUEST;
        };
        return error(status, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DocumentException.class)
    ResponseEntity<ApiErrorResponse> handleDocument(DocumentException exception) {
        var status = switch (exception.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INVALID_STATE -> HttpStatus.BAD_REQUEST;
        };
        return error(status, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AiException.class)
    ResponseEntity<ApiErrorResponse> handleAi(AiException exception) {
        var status = switch (exception.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INVALID_STATE -> HttpStatus.BAD_REQUEST;
            case PROVIDER_UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
        };
        return error(status, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        var fieldErrors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Dữ liệu gửi lên chưa hợp lệ.",
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadableJson() {
        return error(
                HttpStatus.BAD_REQUEST,
                "INVALID_JSON",
                "Dữ liệu JSON không hợp lệ hoặc không dùng mã hóa UTF-8.",
                Map.of()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> handleMaxUploadSize() {
        return error(
                HttpStatus.BAD_REQUEST,
                "DOCUMENT_FILE_TOO_LARGE",
                "File tối đa 20 MB.",
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            Map<String, String> fieldErrors
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(clock),
                status.value(),
                code,
                message,
                fieldErrors
        ));
    }
}

package timmy.todo.server.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import timmy.todo.server.common.TraceIdFilter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 전역 예외 핸들러.
 *
 * <p>AspectLogger는 {@code @AfterThrowing} 어드바이스가 없어 예외 흐름의 OUTPUT을 찍지 못한다.
 * 따라서 이 클래스는 단순 응답 변환이 아니라 <b>출구 로깅 책임</b>을 함께 진다.
 *
 * <p>4xx는 {@code log.warn} + stacktrace 미포함, 5xx는 {@code log.error} + stacktrace 포함.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionController {

    // ========== 4xx — 클라이언트 책임 ==========

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> notFound(NotFoundException e, HttpServletRequest request) {
        return warn(HttpStatus.NOT_FOUND, "NOT_FOUND", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(DuplicatedException.class)
    public ResponseEntity<ExceptionResponseDTO> duplicated(DuplicatedException e, HttpServletRequest request) {
        return warn(HttpStatus.CONFLICT, "DUPLICATED", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionResponseDTO> conflict(ConflictException e, HttpServletRequest request) {
        return warn(HttpStatus.CONFLICT, "CONFLICT", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(BadValidationException.class)
    public ResponseEntity<ExceptionResponseDTO> badValidation(BadValidationException e, HttpServletRequest request) {
        return warn(HttpStatus.BAD_REQUEST, "BAD_VALIDATION", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(UUIDException.class)
    public ResponseEntity<ExceptionResponseDTO> invalidUuid(UUIDException e, HttpServletRequest request) {
        return warn(HttpStatus.BAD_REQUEST, "INVALID_UUID", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponseDTO> forbidden(ForbiddenException e, HttpServletRequest request) {
        return warn(HttpStatus.FORBIDDEN, "FORBIDDEN", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ExceptionResponseDTO> unauthorized(AuthorizationException e, HttpServletRequest request) {
        return warn(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", String.valueOf(e.getInformation()), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDTO> validationFailed(MethodArgumentNotValidException e,
                                                                 HttpServletRequest request) {
        List<ExceptionResponseDTO.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> ExceptionResponseDTO.FieldError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ExceptionResponseDTO body = body(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "입력값 검증에 실패했습니다.", request);
        body.setFieldErrors(fieldErrors);

        log.warn("{} {} {} {} - {}", request.getMethod(), request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(), "VALIDATION_FAILED", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponseDTO> constraintViolation(ConstraintViolationException e,
                                                                    HttpServletRequest request) {
        return warn(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", e.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponseDTO> malformedRequest(HttpMessageNotReadableException e,
                                                                 HttpServletRequest request) {
        return warn(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponseDTO> typeMismatch(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest request) {
        return warn(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", e.getMessage(), request);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ExceptionResponseDTO> noHandler(Exception e, HttpServletRequest request) {
        return warn(HttpStatus.NOT_FOUND, "NO_HANDLER", e.getMessage(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponseDTO> methodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                 HttpServletRequest request) {
        return warn(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", e.getMessage(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ExceptionResponseDTO> unsupportedMediaType(HttpMediaTypeNotSupportedException e,
                                                                     HttpServletRequest request) {
        return warn(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", e.getMessage(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ExceptionResponseDTO> notAcceptable(HttpMediaTypeNotAcceptableException e,
                                                              HttpServletRequest request) {
        return warn(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE", e.getMessage(), request);
    }

    // ========== 5xx — 서버 책임 ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> internalError(Exception e, HttpServletRequest request) {
        String message = e.getClass().getSimpleName() + ": " + e.getMessage();

        log.error("{} {} {} {} - {}", request.getMethod(), request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", message, e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, request));
    }

    // ========== 내부 헬퍼 ==========

    private ResponseEntity<ExceptionResponseDTO> warn(HttpStatus status, String code,
                                                      String message, HttpServletRequest request) {
        log.warn("{} {} {} {} - {}", request.getMethod(), request.getRequestURI(),
                status.value(), code, message);
        return ResponseEntity.status(status).body(body(status, code, message, request));
    }

    private ExceptionResponseDTO body(HttpStatus status, String code,
                                      String message, HttpServletRequest request) {
        return ExceptionResponseDTO.builder()
                .traceId(MDC.get(TraceIdFilter.TRACE_ID))
                .status(status.value())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .build();
    }
}

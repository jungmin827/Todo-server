package timmy.todo.server.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 전역 예외 응답 포맷 (10_LOGGING_AND_EXCEPTION "응답 포맷").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionResponseDTO {

    private String traceId;
    private int status;
    private String code;
    private String message;
    private String path;
    private String method;
    private LocalDateTime timestamp;

    /** Bean Validation 실패 시에만 채운다. */
    private List<FieldError> fieldErrors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}

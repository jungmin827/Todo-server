package timmy.todo.server.exception;

import lombok.Getter;

/**
 * BadValidationException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class BadValidationException extends RuntimeException {

    private final Object information;

    public BadValidationException(Object information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

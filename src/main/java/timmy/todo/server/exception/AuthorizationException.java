package timmy.todo.server.exception;

import lombok.Getter;

/**
 * AuthorizationException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class AuthorizationException extends RuntimeException {

    private final Object information;

    public AuthorizationException(Object information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

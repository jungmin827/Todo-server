package timmy.todo.server.exception;

import lombok.Getter;

/**
 * ForbiddenException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class ForbiddenException extends RuntimeException {

    private final String information;

    public ForbiddenException(String information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

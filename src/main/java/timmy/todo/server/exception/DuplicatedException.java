package timmy.todo.server.exception;

import lombok.Getter;

/**
 * DuplicatedException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class DuplicatedException extends RuntimeException {

    private final String information;

    public DuplicatedException(String information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

package timmy.todo.server.exception;

import lombok.Getter;

/**
 * ConflictException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class ConflictException extends RuntimeException {

    private final String information;

    public ConflictException(String information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

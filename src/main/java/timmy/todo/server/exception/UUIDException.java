package timmy.todo.server.exception;

import lombok.Getter;

/**
 * UUIDException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class UUIDException extends RuntimeException {

    private final String information;

    public UUIDException(String information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

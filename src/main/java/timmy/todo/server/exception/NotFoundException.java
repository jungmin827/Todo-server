package timmy.todo.server.exception;

import lombok.Getter;

/**
 * NotFoundException — {@link ExceptionController}가 HTTP 상태 코드로 변환한다.
 */
@Getter
public class NotFoundException extends RuntimeException {

    private final String information;

    public NotFoundException(String information) {
        super(String.valueOf(information));
        this.information = information;
    }
}

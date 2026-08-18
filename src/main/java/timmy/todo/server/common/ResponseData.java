package timmy.todo.server.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * void 결과 응답 표준 래퍼. DELETE는 204가 아니라 200 + {@code ResponseData<Void>}로 반환한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseData<T> {
    private ResponseDataType result;
    private T data;
}

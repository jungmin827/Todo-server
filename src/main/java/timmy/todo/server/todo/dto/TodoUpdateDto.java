package timmy.todo.server.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 부분 수정용. null로 온 필드는 매퍼의
 * {@code NullValuePropertyMappingStrategy.IGNORE}가 기존 값을 유지한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoUpdateDto {

    @Schema(description = "할 일 제목", example = "장보기")
    @Size(max = 200, message = "제목은 최대 200자입니다.")
    private String title;

    @Schema(description = "할 일 내용", example = "우유, 계란, 빵")
    @Size(max = 2000, message = "내용은 최대 2000자입니다.")
    private String body;

    @Schema(description = "완료 여부", example = "true")
    private Boolean completed;
}

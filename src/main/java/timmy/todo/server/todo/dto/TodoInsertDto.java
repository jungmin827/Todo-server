package timmy.todo.server.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoInsertDto {

    @Schema(description = "할 일 제목", example = "장보기")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 최대 200자입니다.")
    private String title;

    @Schema(description = "할 일 내용", example = "우유, 계란")
    @Size(max = 2000, message = "내용은 최대 2000자입니다.")
    private String body;

    @Schema(description = "완료 여부. 생략하면 false", example = "false")
    private Boolean completed;
}

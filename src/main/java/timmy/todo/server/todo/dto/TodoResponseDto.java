package timmy.todo.server.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 필드 순서는 {@code Todo} Entity 필드 순서와 일치시킨다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoResponseDto {

    @Schema(description = "Todo IDX", example = "1")
    private Long idx;

    @Schema(description = "할 일 제목", example = "장보기")
    private String title;

    @Schema(description = "할 일 내용", example = "우유, 계란")
    private String body;

    @Schema(description = "완료 여부", example = "false")
    private Boolean completed;

    @Schema(description = "등록일")
    private LocalDateTime registerDate;

    @Schema(description = "수정일")
    private LocalDateTime modifyDate;
}

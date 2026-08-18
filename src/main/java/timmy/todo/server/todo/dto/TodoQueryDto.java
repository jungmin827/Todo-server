package timmy.todo.server.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 목록 조회 검색 조건. 날짜는 {@code From}/{@code To} 쌍으로 받는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoQueryDto {

    @Schema(description = "제목 부분 일치")
    private String title;

    @Schema(description = "내용 부분 일치")
    private String body;

    @Schema(description = "완료 여부")
    private Boolean completed;

    @Schema(description = "등록일 검색 시작", example = "2026-08-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registerDateFrom;

    @Schema(description = "등록일 검색 종료", example = "2026-08-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registerDateTo;

    @Schema(description = "수정일 검색 시작", example = "2026-08-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifyDateFrom;

    @Schema(description = "수정일 검색 종료", example = "2026-08-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifyDateTo;
}

package timmy.todo.server.todo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timmy.todo.server.common.ResponseData;
import timmy.todo.server.exception.ExceptionResponseDTO;
import timmy.todo.server.todo.dto.TodoInsertDto;
import timmy.todo.server.todo.dto.TodoQueryDto;
import timmy.todo.server.todo.dto.TodoResponseDto;
import timmy.todo.server.todo.dto.TodoUpdateDto;

import java.util.List;

@Slf4j
@Tag(name = "Todo", description = "Todo API")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/todos")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid input",
                content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Todo not found",
                content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Duplicate data",
                content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class)))
})
public class TodoController {

    private final TodoService todoService;

    // ========== CREATE (생성) ==========

    @Operation(summary = "Todo 등록", description = "Todo를 등록하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponseDto.class)))
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, headers = "X-API-VERSION=1")
    public ResponseEntity<TodoResponseDto> insertTodo(
            @Parameter(description = "Todo 등록 정보", required = true)
            @Valid @RequestBody TodoInsertDto dto) {
        TodoResponseDto responseDto = todoService.insertTodo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // ========== READ (조회) ==========

    @Operation(summary = "Todo 조회", description = "Todo 정보를 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponseDto.class)))
    })
    @GetMapping(value = "/{idx:[0-9]++}", produces = MediaType.APPLICATION_JSON_VALUE, headers = "X-API-VERSION=1")
    public ResponseEntity<TodoResponseDto> getTodoByIdx(
            @Parameter(description = "Todo IDX", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.ok(todoService.getTodoByIdx(idx));
    }

    @Operation(summary = "Todo 목록 조회",
            description = "Todo 전체 목록을 조회하는 API. 검색 조건을 주지 않으면 DB의 모든 Todo를 반환한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoResponseDto.class))))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, headers = "X-API-VERSION=1")
    public ResponseEntity<List<TodoResponseDto>> getTodoList(
            @Parameter(description = "Todo 검색 조건")
            @ModelAttribute TodoQueryDto queryDto) {
        return ResponseEntity.ok(todoService.getTodoList(queryDto));
    }

    // ========== UPDATE (수정) ==========

    @Operation(summary = "Todo 수정", description = "Todo 정보를 수정하는 API. null로 보낸 필드는 기존 값을 유지한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponseDto.class)))
    })
    @PutMapping(value = "/{idx:[0-9]++}", produces = MediaType.APPLICATION_JSON_VALUE, headers = "X-API-VERSION=1")
    public ResponseEntity<TodoResponseDto> updateTodoByIdx(
            @Parameter(description = "Todo IDX", required = true) @PathVariable Long idx,
            @Parameter(description = "수정할 Todo 정보", required = true)
            @Valid @RequestBody TodoUpdateDto dto) {
        return ResponseEntity.ok(todoService.updateTodoByIdx(idx, dto));
    }

    // ========== DELETE (삭제) ==========

    @Operation(summary = "Todo 삭제", description = "Todo를 삭제하는 API")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "삭제 성공")})
    @DeleteMapping(value = "/{idx:[0-9]++}", produces = MediaType.APPLICATION_JSON_VALUE, headers = "X-API-VERSION=1")
    public ResponseEntity<ResponseData<Void>> deleteTodoByIdx(
            @Parameter(description = "Todo IDX", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.ok(todoService.deleteTodoByIdx(idx));
    }
}
